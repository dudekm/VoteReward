package me.adixe.votereward.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * {@link HttpTransport} on the JDK's own {@link HttpClient} (Java 11+), so the plugin
 * needs no HTTP library on any server version.
 */
public final class JdkHttpTransport implements HttpTransport {
    private final HttpClient client;

    public JdkHttpTransport() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public HttpResult get(URI uri, Map<String, String> headers, Duration timeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri).GET().timeout(timeout);
        headers.forEach(request::header);

        HttpResponse<String> response = client.send(request.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        return new HttpResult(response.statusCode(), response.body());
    }
}
