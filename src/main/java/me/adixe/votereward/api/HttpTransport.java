package me.adixe.votereward.api;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

/**
 * Performs HTTP GET requests. Kept behind an interface so the API client can be
 * tested without a network and the HTTP library can be swapped.
 */
public interface HttpTransport {
    HttpResult get(URI uri, Map<String, String> headers, Duration timeout) throws IOException, InterruptedException;
}
