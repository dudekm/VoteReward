package me.adixe.votereward.reward;

import me.adixe.votereward.ledger.PendingReward;
import me.adixe.votereward.ledger.VoteLedger;
import me.adixe.votereward.site.VoteSites;
import me.adixe.votereward.vote.Nickname;
import me.adixe.votereward.vote.Vote;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static me.adixe.votereward.TestData.site;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-25T12:00:00Z");
    private static final Nickname STEVE = Nickname.of("Steve");

    private final VoteLedger ledger = VoteLedger.empty();
    private final Set<Nickname> online = new HashSet<>();
    private final RecordingDispatcher dispatcher = new RecordingDispatcher();
    private final RewardService service = new RewardService(ledger, new VoteSites(List.of(site("a"))),
            online::contains, dispatcher, silentLogger());

    @Test
    void deliversWaitingRewards() {
        ledger.record("a", List.of(Vote.of("u1", "Steve", NOW)), NOW);

        assertEquals(1, service.deliver(STEVE));
        assertEquals(1, dispatcher.given.size());
        assertEquals("a", dispatcher.given.get(0).site().name());
        assertEquals(List.of(), ledger.pendingFor(STEVE));
    }

    @Test
    void nothingToDeliver() {
        assertEquals(0, service.deliver(STEVE));
        assertTrue(dispatcher.given.isEmpty());
    }

    @Test
    void newRewardsGoStraightToOnlinePlayers() {
        online.add(STEVE);

        service.rewardsEarned(ledger.record("a", List.of(Vote.of("u1", "Steve", NOW)), NOW));

        assertEquals(1, dispatcher.given.size());
    }

    @Test
    void offlinePlayersKeepTheirRewardForLater() {
        service.rewardsEarned(ledger.record("a", List.of(Vote.of("u1", "Steve", NOW)), NOW));

        assertTrue(dispatcher.given.isEmpty());
        assertEquals(1, ledger.pendingFor(STEVE).size());
    }

    @Test
    void rewardThatCouldNotBeGivenWaitsAgain() {
        ledger.record("a", List.of(Vote.of("u1", "Steve", NOW)), NOW);
        dispatcher.playerLeft = true;

        service.deliver(STEVE);

        assertEquals(1, ledger.pendingFor(STEVE).size());
    }

    @Test
    void rewardForARemovedListIsDropped() {
        ledger.record("removed", List.of(Vote.of("u1", "Steve", NOW)), NOW);

        assertEquals(0, service.deliver(STEVE));
        assertEquals(List.of(), ledger.pendingFor(STEVE));
    }

    private static Logger silentLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);

        return logger;
    }

    private static final class RecordingDispatcher implements RewardDispatcher {
        final List<Reward> given = new ArrayList<>();
        boolean playerLeft;

        @Override
        public void dispatch(Nickname player, List<Reward> rewards, Consumer<PendingReward> undelivered) {
            if (playerLeft) {
                rewards.forEach(reward -> undelivered.accept(reward.pending()));
            } else {
                given.addAll(rewards);
            }
        }
    }
}
