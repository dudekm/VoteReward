package me.adixe.votereward.vote;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoteTest {
    private static final Instant NOW = Instant.parse("2026-10-25T09:00:00Z");

    @Test
    void voteWithoutNicknameHasNone() {
        assertTrue(Vote.of("id", null, NOW).nickname().isEmpty());
        assertTrue(Vote.of("id", " ", NOW).nickname().isEmpty());
    }

    @Test
    void voteNeedsAnId() {
        assertThrows(IllegalArgumentException.class, () -> Vote.of("", "Steve", NOW));
    }

    @Test
    void voteDayIsTheUtcDate() {
        assertEquals(LocalDate.of(2026, 10, 25), VoteDay.of(Instant.parse("2026-10-25T23:59:59Z")));
        assertEquals(LocalDate.of(2026, 10, 26), VoteDay.of(Instant.parse("2026-10-26T00:00:00Z")));
    }
}
