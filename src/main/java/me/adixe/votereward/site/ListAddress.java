package me.adixe.votereward.site;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

/**
 * Base address of a vote list, e.g. {@code https://lista-minecraft.pl}. Every list
 * exposes the same API under it, so this is all that is needed to reach a server's votes.
 */
public final class ListAddress {
    private final URI uri;

    private ListAddress(URI uri) {
        this.uri = uri;
    }

    public static ListAddress of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("List address is empty.");
        }

        String trimmed = raw.trim();

        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        URI uri;

        try {
            uri = new URI(trimmed);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("List address is not a valid URL: " + raw, exception);
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);

        // http is accepted for local development against a mock or a locally running list.
        if (!scheme.equals("https") && !scheme.equals("http")) {
            throw new IllegalArgumentException("List address must start with https:// — got: " + raw);
        }

        if (uri.getHost() == null) {
            throw new IllegalArgumentException("List address has no host: " + raw);
        }

        if (uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("List address must not contain ? or #: " + raw);
        }

        return new ListAddress(uri);
    }

    public URI uri() {
        return uri;
    }

    public String host() {
        return uri.getHost();
    }

    /** Resolves a path such as {@code /api/v1/servers/…/votes} against this list. */
    public URI resolve(String absolutePath) {
        String base = uri.toString();

        return URI.create(base + (absolutePath.startsWith("/") ? absolutePath : "/" + absolutePath));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ListAddress address && uri.equals(address.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
