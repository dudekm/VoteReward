package me.adixe.votereward.ledger;

import me.adixe.votereward.vote.Nickname;
import me.adixe.votereward.vote.Vote;
import me.adixe.votereward.vote.VoteDay;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The plugin's memory of votes: how far each list has been read, who was rewarded on which
 * day, and which rewards still wait for their player.
 *
 * <p>Rules it guarantees:
 * <ul>
 *     <li>every vote is turned into a reward at most once, even when a check sees it again;</li>
 *     <li>a nickname is rewarded at most once per list per vote day (UTC), like the lists
 *     themselves allow one vote a day;</li>
 *     <li>a reward is handed out at most once — it leaves the ledger before it is given.</li>
 * </ul>
 * Thread-safe: checks run off the main thread while players join and claim on it.
 */
public final class VoteLedger {
    /**
     * Every check overlaps the previous one by this much, so a vote that shows up late with
     * an older timestamp (clock drift between the list and this server, a slow database
     * commit) is still found. Votes read twice are recognised by their ID.
     */
    public static final Duration SAFETY_MARGIN = Duration.ofMinutes(5);

    /** How far back the very first check of a list looks. */
    public static final Duration INITIAL_LOOKBACK = Duration.ofHours(1);

    /** The furthest back a check reaches, e.g. after the server was offline for a while. */
    public static final Duration MAX_CATCH_UP = Duration.ofHours(24);

    /** Rewards nobody collected for this long are dropped. */
    public static final Duration PENDING_EXPIRY = Duration.ofDays(7);

    /** Days of "already rewarded" history kept: today and yesterday cover the UTC day boundary. */
    private static final int REWARDED_DAYS_KEPT = 2;

    private final Map<String, SiteCursor> cursors;
    private final Map<LocalDate, Map<String, Set<String>>> rewarded;
    private final List<PendingReward> pending;
    private boolean dirty;

    private VoteLedger(LedgerSnapshot snapshot) {
        this.cursors = new HashMap<>(snapshot.cursors());
        this.rewarded = new HashMap<>();
        snapshot.rewarded().forEach((day, sites) -> {
            Map<String, Set<String>> copy = new HashMap<>();
            sites.forEach((site, nicknames) -> copy.put(site, new HashSet<>(nicknames)));
            rewarded.put(day, copy);
        });
        this.pending = new ArrayList<>(snapshot.pending());
    }

    public static VoteLedger empty() {
        return new VoteLedger(LedgerSnapshot.EMPTY);
    }

    public static VoteLedger restore(LedgerSnapshot snapshot) {
        return new VoteLedger(snapshot);
    }

    /**
     * Where the next check of {@code site} starts: where the previous one left off, or
     * {@link #INITIAL_LOOKBACK} ago for a list never checked — but never further back
     * than {@link #MAX_CATCH_UP}.
     */
    public synchronized Instant checkFrom(String site, Instant now) {
        Instant oldestAllowed = now.minus(MAX_CATCH_UP);
        Instant from = Optional.ofNullable(cursors.get(site))
                .map(SiteCursor::position)
                .orElse(now.minus(INITIAL_LOOKBACK));

        return from.isBefore(oldestAllowed) ? oldestAllowed : from;
    }

    /**
     * Records the result of a check and returns the rewards it earned.
     *
     * @param votes     votes returned by the list, in any order
     * @param checkedAt when the check was made
     */
    public synchronized List<PendingReward> record(String site, List<Vote> votes, Instant checkedAt) {
        SiteCursor cursor = cursors.get(site);
        Map<String, Instant> seen = new HashMap<>(cursor == null ? Map.of() : cursor.recentVotes());
        List<PendingReward> earned = new ArrayList<>();
        Instant latest = null;

        List<Vote> ordered = new ArrayList<>(votes);
        ordered.sort(Comparator.comparing(Vote::castAt));

        for (Vote vote : ordered) {
            if (latest == null || vote.castAt().isAfter(latest)) {
                latest = vote.castAt();
            }

            if (seen.putIfAbsent(vote.id(), vote.castAt()) != null) {
                continue;
            }

            if (vote.nickname().isEmpty()) {
                continue;
            }

            Nickname nickname = vote.nickname().get();
            Set<String> rewardedThatDay = rewarded
                    .computeIfAbsent(VoteDay.of(vote.castAt()), day -> new HashMap<>())
                    .computeIfAbsent(site, name -> new HashSet<>());

            if (!rewardedThatDay.add(nickname.key())) {
                continue;
            }

            PendingReward reward = new PendingReward(vote.id(), site, nickname, vote.castAt());
            pending.add(reward);
            earned.add(reward);
        }

        // Start the next check SAFETY_MARGIN before this one — or at the newest vote returned,
        // if that is older: the source may have returned only the oldest part of a backlog.
        Instant position = checkedAt.minus(SAFETY_MARGIN);

        if (latest != null && latest.isBefore(position)) {
            position = latest;
        }

        // The next query includes the cursor's own second, so only votes from that second on can come back.
        Instant keepFrom = position.truncatedTo(ChronoUnit.SECONDS);
        seen.values().removeIf(castAt -> castAt.isBefore(keepFrom));

        cursors.put(site, new SiteCursor(position, seen));
        pruneRewardedDays(VoteDay.of(checkedAt));
        dirty = true;

        return earned;
    }

    /** Removes and returns every reward waiting for {@code nickname}, oldest first. */
    public synchronized List<PendingReward> takePending(Nickname nickname) {
        List<PendingReward> taken = new ArrayList<>();
        Iterator<PendingReward> iterator = pending.iterator();

        while (iterator.hasNext()) {
            PendingReward reward = iterator.next();

            if (reward.nickname().equals(nickname)) {
                taken.add(reward);
                iterator.remove();
            }
        }

        if (!taken.isEmpty()) {
            dirty = true;
        }

        return taken;
    }

    /** Puts back a reward that could not be given after all, e.g. the player left meanwhile. */
    public synchronized void returnPending(PendingReward reward) {
        if (!pending.contains(reward)) {
            pending.add(reward);
            pending.sort(Comparator.comparing(PendingReward::castAt));
            dirty = true;
        }
    }

    public synchronized List<PendingReward> pendingFor(Nickname nickname) {
        List<PendingReward> result = new ArrayList<>();

        for (PendingReward reward : pending) {
            if (reward.nickname().equals(nickname)) {
                result.add(reward);
            }
        }

        return result;
    }

    /** Whether {@code nickname} earned a reward on {@code site} for a vote cast on {@code day}. */
    public synchronized boolean wasRewarded(String site, Nickname nickname, LocalDate day) {
        return rewarded.getOrDefault(day, Map.of()).getOrDefault(site, Set.of()).contains(nickname.key());
    }

    /** Drops rewards nobody collected within {@link #PENDING_EXPIRY}; returns them for logging. */
    public synchronized List<PendingReward> expirePending(Instant now) {
        Instant oldestKept = now.minus(PENDING_EXPIRY);
        List<PendingReward> expired = new ArrayList<>();
        Iterator<PendingReward> iterator = pending.iterator();

        while (iterator.hasNext()) {
            PendingReward reward = iterator.next();

            if (reward.castAt().isBefore(oldestKept)) {
                expired.add(reward);
                iterator.remove();
            }
        }

        if (!expired.isEmpty()) {
            dirty = true;
        }

        return expired;
    }

    /** Forgets lists that are no longer configured, so their state does not pile up. */
    public synchronized void retainSites(Set<String> sites) {
        if (cursors.keySet().retainAll(sites)) {
            dirty = true;
        }
    }

    public synchronized LedgerSnapshot snapshot() {
        Map<LocalDate, Map<String, Set<String>>> rewardedCopy = new HashMap<>();
        rewarded.forEach((day, sites) -> {
            Map<String, Set<String>> copy = new HashMap<>();
            sites.forEach((site, nicknames) -> copy.put(site, Set.copyOf(nicknames)));
            rewardedCopy.put(day, copy);
        });

        return new LedgerSnapshot(cursors, rewardedCopy, pending);
    }

    /** Whether anything changed since the last {@link #markSaved()}. */
    public synchronized boolean isDirty() {
        return dirty;
    }

    public synchronized void markSaved() {
        dirty = false;
    }

    private void pruneRewardedDays(LocalDate today) {
        LocalDate oldestKept = today.minusDays(REWARDED_DAYS_KEPT - 1L);
        rewarded.keySet().removeIf(day -> day.isBefore(oldestKept));
    }
}
