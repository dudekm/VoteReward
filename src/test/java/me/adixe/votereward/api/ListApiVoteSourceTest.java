package me.adixe.votereward.api;

import me.adixe.votereward.vote.Vote;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static me.adixe.votereward.TestData.SERVER_ID;
import static me.adixe.votereward.TestData.page;
import static me.adixe.votereward.TestData.site;
import static me.adixe.votereward.TestData.voteJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListApiVoteSourceTest {
    private static final Instant SINCE = Instant.parse("2026-10-25T09:00:00.123Z");

    private final FakeHttpTransport transport = new FakeHttpTransport();
    private final ListApiVoteSource source = new ListApiVoteSource(transport, new VotePageParser(), "VoteReward/test");

    @Test
    void asksOnlyForVotesSinceTheGivenMomentOldestFirst() throws VoteSourceException {
        transport.respond(200, page(null));

        source.votesSince(site("a"), SINCE);

        assertEquals("https://lista-minecraft.pl/api/v1/servers/" + SERVER_ID + "/votes"
                        + "?createdAt%5Bafter%5D=2026-10-25T09%3A00%3A00Z&order%5BcreatedAt%5D=asc",
                transport.requests.get(0).toString());
        assertEquals("application/ld+json", transport.headers.get(0).get("Accept"));
        assertEquals("VoteReward/test", transport.headers.get(0).get("User-Agent"));
    }

    @Test
    void followsNextPages() throws VoteSourceException {
        transport.respond(200, page("/api/v1/servers/x/votes?page=2", voteJson("u1", "Steve", "2026-10-25T09:00:01Z")))
                .respond(200, page(null, voteJson("u2", "Alex", "2026-10-25T09:00:02Z")));

        List<Vote> votes = source.votesSince(site("a"), SINCE);

        assertEquals(List.of("u1", "u2"), votes.stream().map(Vote::id).toList());
        assertEquals("https://lista-minecraft.pl/api/v1/servers/x/votes?page=2", transport.requests.get(1).toString());
    }

    @Test
    void stopsAfterMaxPages() throws VoteSourceException {
        for (int i = 0; i < ListApiVoteSource.MAX_PAGES; i++) {
            transport.respond(200, page("/next?page=" + (i + 2), voteJson("u" + i, "P" + i, "2026-10-25T09:00:01Z")));
        }

        assertEquals(ListApiVoteSource.MAX_PAGES, source.votesSince(site("a"), SINCE).size());
        assertEquals(ListApiVoteSource.MAX_PAGES, transport.requests.size());
    }

    @Test
    void neverFollowsLinksToAnotherHost() throws VoteSourceException {
        transport.respond(200, page("https://evil.example/steal", voteJson("u1", "Steve", "2026-10-25T09:00:01Z")));

        assertEquals(1, source.votesSince(site("a"), SINCE).size());
        assertEquals(1, transport.requests.size());
    }

    @Test
    void reportsRateLimiting() {
        transport.respond(429, "");

        VoteSourceException exception = assertThrows(VoteSourceException.class,
                () -> source.votesSince(site("a"), SINCE));
        assertTrue(exception.getMessage().contains("429"));
    }

    @Test
    void reportsHttpErrors() {
        transport.respond(500, "oops");

        assertThrows(VoteSourceException.class, () -> source.votesSince(site("a"), SINCE));
    }

    @Test
    void reportsNetworkErrors() {
        transport.fail(new IOException("Connection refused"));

        VoteSourceException exception = assertThrows(VoteSourceException.class,
                () -> source.votesSince(site("a"), SINCE));
        assertTrue(exception.getMessage().contains("Connection refused"));
    }
}
