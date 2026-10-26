package me.adixe.votereward.api;

import com.sun.net.httpserver.HttpServer;
import me.adixe.votereward.vote.Vote;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static me.adixe.votereward.TestData.SERVER_ID;
import static me.adixe.votereward.TestData.page;
import static me.adixe.votereward.TestData.site;
import static me.adixe.votereward.TestData.voteJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Real HTTP round trip against a local server imitating the vote list API.
 */
class ListApiVoteSourceIntegrationTest {
    private final List<String> queries = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private String address;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/api/v1/servers/" + SERVER_ID + "/votes", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            queries.add(query);

            String body = query.endsWith("page=2")
                    ? page(null, voteJson("u2", "Alex", "2026-10-25T09:00:02+00:00"))
                    : page("/api/v1/servers/" + SERVER_ID + "/votes?" + query + "&page=2",
                    voteJson("u1", "Steve", "2026-10-25T09:00:01+00:00"));

            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/ld+json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
        address = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void fetchesAllPagesOverHttp() throws VoteSourceException {
        ListApiVoteSource source = new ListApiVoteSource(new JdkHttpTransport(), new VotePageParser(), "test");

        List<Vote> votes = source.votesSince(site("local", address), Instant.parse("2026-10-25T09:00:00Z"));

        assertEquals(List.of("u1", "u2"), votes.stream().map(Vote::id).toList());
        assertEquals("createdAt%5Bafter%5D=2026-10-25T09%3A00%3A00Z&order%5BcreatedAt%5D=asc", queries.get(0));
    }

    @Test
    void unknownPathIsAnError() {
        ListApiVoteSource source = new ListApiVoteSource(new JdkHttpTransport(), new VotePageParser(), "test");

        // The JDK server answers 404 for paths without a handler, like the list does for unknown routes.
        assertThrows(VoteSourceException.class,
                () -> source.votesSince(site("local", address + "/nothing-here"), Instant.now()));
    }
}
