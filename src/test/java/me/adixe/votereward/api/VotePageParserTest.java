package me.adixe.votereward.api;

import me.adixe.votereward.vote.Nickname;
import me.adixe.votereward.vote.Vote;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static me.adixe.votereward.TestData.page;
import static me.adixe.votereward.TestData.voteJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VotePageParserTest {
    private final VotePageParser parser = new VotePageParser();

    @Test
    void readsJsonLdCollection() throws VoteSourceException {
        VotePage page = parser.parse(page("/api/v1/servers/x/votes?page=2",
                voteJson("u1", "Steve", "2026-10-25T09:06:41+00:00"),
                voteJson("u2", "Alex", "2026-10-25T09:06:41.768Z")));

        assertEquals(2, page.votes().size());

        Vote first = page.votes().get(0);
        assertEquals("u1", first.id());
        assertEquals(Optional.of(Nickname.of("Steve")), first.nickname());
        assertEquals(Instant.parse("2026-10-25T09:06:41Z"), first.castAt());
        assertEquals(Instant.parse("2026-10-25T09:06:41.768Z"), page.votes().get(1).castAt());
        assertEquals(Optional.of("/api/v1/servers/x/votes?page=2"), page.next());
    }

    @Test
    void lastPageHasNoNext() throws VoteSourceException {
        assertTrue(parser.parse(page(null, voteJson("u1", "Steve", "2026-10-25T09:00:00Z"))).next().isEmpty());
    }

    @Test
    void readsHydraPrefixedNamesOfOlderApiPlatform() throws VoteSourceException {
        VotePage page = parser.parse("{\"hydra:member\":[" + voteJson("u1", "Steve", "2026-10-25T09:00:00Z")
                + "],\"hydra:view\":{\"hydra:next\":\"/next\"}}");

        assertEquals(1, page.votes().size());
        assertEquals(Optional.of("/next"), page.next());
    }

    @Test
    void readsPlainJsonArray() throws VoteSourceException {
        VotePage page = parser.parse("[" + voteJson("u1", "Steve", "2026-10-25T09:00:00Z") + "]");

        assertEquals(1, page.votes().size());
        assertTrue(page.next().isEmpty());
    }

    @Test
    void keepsVotesWithoutNickname() throws VoteSourceException {
        VotePage page = parser.parse(page(null, voteJson("u1", null, "2026-10-25T09:00:00Z")));

        assertTrue(page.votes().get(0).nickname().isEmpty());
    }

    @Test
    void readsDateWithoutOffsetAsUtc() throws VoteSourceException {
        VotePage page = parser.parse(page(null, voteJson("u1", "Steve", "2026-10-25T09:00:00")));

        assertEquals(Instant.parse("2026-10-25T09:00:00Z"), page.votes().get(0).castAt());
    }

    @Test
    void skipsEntriesWithoutIdOrDate() throws VoteSourceException {
        VotePage page = parser.parse(page(null,
                "{\"nickname\":\"NoId\",\"createdAt\":\"2026-10-25T09:00:00Z\"}",
                voteJson("u2", "BadDate", "yesterday"),
                "42",
                voteJson("u3", "Ok", "2026-10-25T09:00:00Z")));

        assertEquals(1, page.votes().size());
        assertEquals("u3", page.votes().get(0).id());
    }

    @Test
    void rejectsGarbage() {
        assertThrows(VoteSourceException.class, () -> parser.parse("<html>Bad gateway</html>"));
        assertThrows(VoteSourceException.class, () -> parser.parse("{\"title\":\"An error occurred\"}"));
        assertThrows(VoteSourceException.class, () -> parser.parse("\"text\""));
    }
}
