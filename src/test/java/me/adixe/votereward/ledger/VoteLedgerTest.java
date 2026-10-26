package me.adixe.votereward.ledger;

import me.adixe.votereward.vote.Nickname;
import me.adixe.votereward.vote.Vote;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoteLedgerTest {
    private static final Instant NOW = Instant.parse("2026-10-25T12:00:00Z");
    private static final Nickname STEVE = Nickname.of("Steve");

    private final VoteLedger ledger = VoteLedger.empty();

    @Test
    void firstCheckLooksBackOneHour() {
        assertEquals(NOW.minus(VoteLedger.INITIAL_LOOKBACK), ledger.checkFrom("a", NOW));
    }

    @Test
    void nextCheckOverlapsThePreviousOneBySafetyMargin() {
        ledger.record("a", List.of(), NOW);

        assertEquals(NOW.minus(VoteLedger.SAFETY_MARGIN), ledger.checkFrom("a", NOW.plusSeconds(30)));
    }

    @Test
    void checkAfterLongDowntimeReachesBackAtMostMaxCatchUp() {
        ledger.record("a", List.of(), NOW);
        Instant muchLater = NOW.plus(Duration.ofDays(3));

        assertEquals(muchLater.minus(VoteLedger.MAX_CATCH_UP), ledger.checkFrom("a", muchLater));
    }

    @Test
    void continuesFromTheNewestVoteWhenOnlyPartOfABacklogWasRead() {
        Instant old = NOW.minus(Duration.ofHours(3));
        ledger.record("a", List.of(vote("u1", "Steve", old)), NOW);

        assertEquals(old, ledger.checkFrom("a", NOW));
    }

    @Test
    void voteEarnsAPendingReward() {
        List<PendingReward> earned = ledger.record("a", List.of(vote("u1", "Steve", NOW)), NOW);

        assertEquals(1, earned.size());
        assertEquals(earned, ledger.pendingFor(Nickname.of("steve")));
        assertTrue(ledger.wasRewarded("a", STEVE, LocalDate.of(2026, 10, 25)));
    }

    @Test
    void sameVoteSeenTwiceCountsOnce() {
        ledger.record("a", List.of(vote("u1", "Steve", NOW)), NOW);

        assertEquals(List.of(), ledger.record("a", List.of(vote("u1", "Steve", NOW)), NOW.plusSeconds(30)));
        assertEquals(1, ledger.pendingFor(STEVE).size());
    }

    @Test
    void oneRewardPerNicknamePerListPerDay() {
        ledger.record("a", List.of(vote("u1", "Steve", NOW), vote("u2", "STEVE", NOW.plusSeconds(60))), NOW);

        assertEquals(1, ledger.pendingFor(STEVE).size());
    }

    @Test
    void nextUtcDayEarnsAgain() {
        ledger.record("a", List.of(vote("u1", "Steve", NOW)), NOW);
        Instant tomorrow = NOW.plus(Duration.ofDays(1));

        assertEquals(1, ledger.record("a", List.of(vote("u2", "Steve", tomorrow)), tomorrow).size());
    }

    @Test
    void listsAreCountedSeparately() {
        ledger.record("a", List.of(vote("u1", "Steve", NOW)), NOW);
        ledger.record("b", List.of(vote("u2", "Steve", NOW)), NOW);

        assertEquals(2, ledger.pendingFor(STEVE).size());
    }

    @Test
    void voteWithoutNicknameEarnsNothing() {
        assertEquals(List.of(), ledger.record("a", List.of(vote("u1", null, NOW)), NOW));
    }

    @Test
    void takingPendingRemovesItAndReturningPutsItBack() {
        ledger.record("a", List.of(vote("u1", "Steve", NOW)), NOW);

        List<PendingReward> taken = ledger.takePending(STEVE);

        assertEquals(1, taken.size());
        assertEquals(List.of(), ledger.takePending(STEVE));

        ledger.returnPending(taken.get(0));
        ledger.returnPending(taken.get(0));

        assertEquals(1, ledger.pendingFor(STEVE).size());
    }

    @Test
    void uncollectedRewardsExpire() {
        ledger.record("a", List.of(vote("u1", "Steve", NOW)), NOW);

        assertEquals(List.of(), ledger.expirePending(NOW.plus(Duration.ofDays(1))));
        assertEquals(1, ledger.expirePending(NOW.plus(VoteLedger.PENDING_EXPIRY).plusSeconds(1)).size());
        assertEquals(List.of(), ledger.pendingFor(STEVE));
    }

    @Test
    void removedListsAreForgotten() {
        ledger.record("a", List.of(), NOW);
        ledger.record("b", List.of(), NOW);

        ledger.retainSites(Set.of("a"));

        assertEquals(Set.of("a"), ledger.snapshot().cursors().keySet());
    }

    @Test
    void tracksUnsavedChanges() {
        assertFalse(ledger.isDirty());

        ledger.record("a", List.of(), NOW);
        assertTrue(ledger.isDirty());

        ledger.markSaved();
        assertFalse(ledger.isDirty());
    }

    @Test
    void survivesSnapshotRoundTrip() {
        ledger.record("a", List.of(vote("u1", "Steve", NOW)), NOW);

        VoteLedger restored = VoteLedger.restore(ledger.snapshot());

        assertEquals(1, restored.pendingFor(STEVE).size());
        assertEquals(ledger.checkFrom("a", NOW), restored.checkFrom("a", NOW));
        assertEquals(List.of(), restored.record("a", List.of(vote("u1", "Steve", NOW)), NOW));
    }

    private static Vote vote(String id, String nickname, Instant castAt) {
        return Vote.of(id, nickname, castAt);
    }
}
