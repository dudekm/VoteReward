package me.adixe.votereward.api;

import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.vote.Vote;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link VoteSource} backed by the vote lists' REST API:
 * {@code GET {address}/api/v1/servers/{uuid}/votes}.
 *
 * <p>Only new votes are requested — {@code createdAt[after]} narrows the query to the time
 * since the last check and {@code order[createdAt]=asc} returns them oldest first — so a
 * check costs the list one small query instead of the server's whole vote history.
 * Pages are followed through {@code view.next}, at most {@link #MAX_PAGES} per call; a larger
 * backlog is picked up by the next call, which starts from the newest vote returned here.
 */
public final class ListApiVoteSource implements VoteSource {
    public static final String VOTES_PATH = "/api/v1/servers/%s/votes";

    /** Pages read per check; the API serves 30 votes a page, so 600 votes per list per check. */
    public static final int MAX_PAGES = 20;

    public static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpTransport transport;
    private final VotePageParser parser;
    private final String userAgent;

    public ListApiVoteSource(HttpTransport transport, VotePageParser parser, String userAgent) {
        this.transport = transport;
        this.parser = parser;
        this.userAgent = userAgent;
    }

    @Override
    public List<Vote> votesSince(VoteSite site, Instant since) throws VoteSourceException {
        List<Vote> votes = new ArrayList<>();
        URI uri = firstPage(site, since);

        for (int page = 0; page < MAX_PAGES && uri != null; page++) {
            VotePage result = fetch(site, uri);
            votes.addAll(result.votes());
            uri = result.next().map(next -> resolveNext(site, next)).orElse(null);
        }

        return votes;
    }

    URI firstPage(VoteSite site, Instant since) {
        Map<String, String> query = new LinkedHashMap<>();
        // Inclusive: votes in the same second as the last one seen are re-read and de-duplicated by ID.
        query.put("createdAt[after]", since.truncatedTo(ChronoUnit.SECONDS).toString());
        query.put("order[createdAt]", "asc");

        return site.address().resolve(String.format(VOTES_PATH, site.serverId()) + "?" + encode(query));
    }

    private VotePage fetch(VoteSite site, URI uri) throws VoteSourceException {
        HttpResult response;

        try {
            response = transport.get(uri, headers(), TIMEOUT);
        } catch (IOException exception) {
            throw new VoteSourceException("Could not reach " + site.address() + ": " + describe(exception), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new VoteSourceException("Interrupted while querying " + site.address() + ".", exception);
        }

        if (response.status() == 429) {
            throw new VoteSourceException(site.address() + " is rate limiting requests (HTTP 429)"
                    + " — increase polling.interval.");
        }

        if (!response.isSuccessful()) {
            throw new VoteSourceException(site.address() + " answered HTTP " + response.status() + ".");
        }

        return parser.parse(response.body());
    }

    /** Follows only links on the same list, never to another host. */
    private static URI resolveNext(VoteSite site, String next) {
        URI base = site.address().uri();
        URI resolved = base.resolve(next);

        if (!base.getHost().equalsIgnoreCase(resolved.getHost())) {
            return null;
        }

        return resolved;
    }

    private Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/ld+json");
        headers.put("User-Agent", userAgent);

        return headers;
    }

    private static String encode(Map<String, String> query) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : query.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }

            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return builder.toString();
    }

    private static String describe(IOException exception) {
        return Optional.ofNullable(exception.getMessage()).orElse(exception.getClass().getSimpleName());
    }
}
