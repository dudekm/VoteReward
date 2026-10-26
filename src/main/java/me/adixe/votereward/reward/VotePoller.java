package me.adixe.votereward.reward;

import me.adixe.votereward.api.VoteSource;
import me.adixe.votereward.api.VoteSourceException;
import me.adixe.votereward.config.Durations;
import me.adixe.votereward.config.Settings;
import me.adixe.votereward.ledger.LedgerRepository;
import me.adixe.votereward.ledger.PendingReward;
import me.adixe.votereward.ledger.VoteLedger;
import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.vote.Vote;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * One check of every configured list: fetch new votes, record them in the ledger, save it,
 * and hand the earned rewards to the {@link RewardService}. Run every
 * {@code polling.interval} off the main thread.
 *
 * <p>A list that cannot be reached is logged once when it starts failing and once when it
 * recovers, not on every check, so an outage does not flood the console.
 */
public final class VotePoller {
    private final Settings settings;
    private final VoteSource source;
    private final VoteLedger ledger;
    private final LedgerRepository repository;
    private final RewardService rewards;
    private final Clock clock;
    private final Logger logger;

    private final AtomicBoolean running = new AtomicBoolean();
    private final Set<String> failingSites = new HashSet<>();

    public VotePoller(Settings settings, VoteSource source, VoteLedger ledger, LedgerRepository repository,
                      RewardService rewards, Clock clock, Logger logger) {
        this.settings = settings;
        this.source = source;
        this.ledger = ledger;
        this.repository = repository;
        this.rewards = rewards;
        this.clock = clock;
        this.logger = logger;
    }

    public PollReport poll() {
        if (!running.compareAndSet(false, true)) {
            return PollReport.SKIPPED;
        }

        try {
            return pollAllSites();
        } finally {
            running.set(false);
        }
    }

    private PollReport pollAllSites() {
        List<PendingReward> earned = new ArrayList<>();
        int checked = 0;
        int failed = 0;
        int read = 0;

        for (VoteSite site : settings.sites().all()) {
            Instant now = clock.instant();

            try {
                List<Vote> votes = source.votesSince(site, ledger.checkFrom(site.name(), now));
                earned.addAll(ledger.record(site.name(), votes, now));
                read += votes.size();
                checked++;
                recovered(site);
            } catch (VoteSourceException exception) {
                failed++;
                failing(site, exception);
            } catch (RuntimeException exception) {
                failed++;
                logger.log(Level.SEVERE, "Unexpected error while checking votes on " + site.name() + ".", exception);
            }
        }

        for (PendingReward expired : ledger.expirePending(clock.instant())) {
            logger.info("Dropped " + expired.nickname() + "'s uncollected reward for a vote on "
                    + expired.site() + " (not collected within "
                    + Durations.format(VoteLedger.PENDING_EXPIRY) + ").");
        }

        save();

        if (!earned.isEmpty()) {
            rewards.rewardsEarned(earned);
        }

        return new PollReport(checked, failed, read, earned.size(), false);
    }

    /** Writes the ledger if it changed. Also called on shutdown. */
    public void save() {
        if (!ledger.isDirty()) {
            return;
        }

        try {
            repository.save(ledger.snapshot());
            ledger.markSaved();
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Could not save vote data.", exception);
        }
    }

    private void failing(VoteSite site, VoteSourceException exception) {
        synchronized (failingSites) {
            if (failingSites.add(site.name())) {
                logger.warning("Could not check votes on " + site.name() + ": " + exception.getMessage()
                        + " Retrying every " + Durations.format(settings.pollInterval())
                        + "; votes cast meanwhile will be picked up.");
            } else {
                logger.log(Level.FINE, "Still cannot check votes on " + site.name() + ".", exception);
            }
        }
    }

    private void recovered(VoteSite site) {
        synchronized (failingSites) {
            if (failingSites.remove(site.name())) {
                logger.info("Checking votes on " + site.name() + " works again.");
            }
        }
    }
}
