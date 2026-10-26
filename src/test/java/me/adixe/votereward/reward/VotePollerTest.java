package me.adixe.votereward.reward;

import me.adixe.votereward.api.VoteSource;
import me.adixe.votereward.api.VoteSourceException;
import me.adixe.votereward.config.Settings;
import me.adixe.votereward.ledger.LedgerRepository;
import me.adixe.votereward.ledger.LedgerSnapshot;
import me.adixe.votereward.ledger.VoteLedger;
import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.site.VoteSites;
import me.adixe.votereward.vote.Nickname;
import me.adixe.votereward.vote.Vote;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static me.adixe.votereward.TestData.site;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VotePollerTest {
    private static final Instant NOW = Instant.parse("2026-10-25T12:00:00Z");

    private final VoteLedger ledger = VoteLedger.empty();
    private final FakeSource source = new FakeSource();
    private final SavingRepository repository = new SavingRepository();
    private final List<Nickname> rewarded = new ArrayList<>();
    private final List<LogRecord> logs = new ArrayList<>();
    private final Logger logger = Logger.getAnonymousLogger();

    VotePollerTest() {
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                logs.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
    }

    @Test
    void checksEveryListFromItsCursorAndRewardsOnlinePlayers() {
        source.votes.put("a", List.of(Vote.of("u1", "Steve", NOW.minusSeconds(10))));
        source.votes.put("b", List.of());

        PollReport report = poller(site("a"), site("b", "https://b.pl")).poll();

        assertEquals(new PollReport(2, 0, 1, 1, false), report);
        assertEquals(NOW.minus(VoteLedger.INITIAL_LOOKBACK), source.asked.get("a"));
        assertEquals(List.of(Nickname.of("Steve")), rewarded);
        assertEquals(1, repository.saves);
    }

    @Test
    void oneFailingListDoesNotStopTheOthers() {
        source.votes.put("b", List.of(Vote.of("u1", "Steve", NOW)));

        PollReport report = poller(site("a"), site("b", "https://b.pl")).poll();

        assertEquals(1, report.sitesChecked());
        assertEquals(1, report.sitesFailed());
        assertEquals(1, report.rewardsEarned());
    }

    @Test
    void failingListIsLoggedOnceUntilItRecovers() {
        VotePoller poller = poller(site("a"));

        poller.poll();
        poller.poll();
        assertEquals(1, logs.stream().filter(log -> log.getLevel() == Level.WARNING).count());

        source.votes.put("a", List.of());
        poller.poll();
        assertEquals(1, logs.stream().filter(log -> log.getLevel() == Level.INFO).count());
    }

    @Test
    void failedListKeepsItsPositionSoNoVoteIsMissed() {
        VotePoller poller = poller(site("a"));

        poller.poll();
        poller.poll();

        assertNull(ledger.snapshot().cursors().get("a"));
    }

    @Test
    void doesNotSaveWhenNothingChanged() {
        VotePoller poller = poller(site("a"));

        poller.poll();

        assertEquals(0, repository.saves);
    }

    private VotePoller poller(VoteSite... sites) {
        VoteSites voteSites = new VoteSites(List.of(sites));
        RewardService rewards = new RewardService(ledger, voteSites, nickname -> true,
                (player, given, undelivered) -> rewarded.add(player), logger);

        return new VotePoller(new Settings(Duration.ofSeconds(30), voteSites), source, ledger, repository, rewards,
                Clock.fixed(NOW, ZoneOffset.UTC), logger);
    }

    /** Lists without prepared votes fail, like an unreachable list. */
    private static final class FakeSource implements VoteSource {
        final Map<String, List<Vote>> votes = new HashMap<>();
        final Map<String, Instant> asked = new HashMap<>();

        @Override
        public List<Vote> votesSince(VoteSite site, Instant since) throws VoteSourceException {
            asked.put(site.name(), since);
            List<Vote> result = votes.get(site.name());

            if (result == null) {
                throw new VoteSourceException("unreachable");
            }

            return result;
        }
    }

    private static final class SavingRepository implements LedgerRepository {
        int saves;

        @Override
        public LedgerSnapshot load() {
            return LedgerSnapshot.EMPTY;
        }

        @Override
        public void save(LedgerSnapshot snapshot) {
            saves++;
        }
    }
}
