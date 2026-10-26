package me.adixe.votereward.api;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Answers requests with queued responses and remembers what was asked.
 */
final class FakeHttpTransport implements HttpTransport {
    final List<URI> requests = new ArrayList<>();
    final List<Map<String, String>> headers = new ArrayList<>();
    private final Deque<Object> responses = new ArrayDeque<>();

    FakeHttpTransport respond(int status, String body) {
        responses.add(new HttpResult(status, body));

        return this;
    }

    FakeHttpTransport fail(IOException exception) {
        responses.add(exception);

        return this;
    }

    @Override
    public HttpResult get(URI uri, Map<String, String> headers, Duration timeout) throws IOException {
        requests.add(uri);
        this.headers.add(headers);

        Object response = responses.poll();

        if (response instanceof IOException exception) {
            throw exception;
        }

        if (response == null) {
            throw new AssertionError("Unexpected request: " + uri);
        }

        return (HttpResult) response;
    }
}
