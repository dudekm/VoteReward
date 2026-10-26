package me.adixe.votereward.vote;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier of a server on a vote list — the UUID shown in the list's server panel
 * and in the last segment of the server's page address.
 */
public record ServerId(UUID value) {
    public ServerId {
        Objects.requireNonNull(value, "value");
    }

    public static ServerId of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Server ID is empty.");
        }

        try {
            return new ServerId(UUID.fromString(raw.trim()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Server ID is not a valid UUID: " + raw, exception);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
