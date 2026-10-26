package me.adixe.votereward.vote;

import java.util.Locale;
import java.util.Objects;

/**
 * A player name as typed on the vote list. Minecraft names are case-insensitive,
 * so two nicknames are equal when they differ only in letter case.
 */
public final class Nickname {
    private final String value;
    private final String key;

    private Nickname(String value) {
        this.value = value;
        this.key = value.toLowerCase(Locale.ROOT);
    }

    public static Nickname of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Nickname is empty.");
        }

        return new Nickname(raw.trim());
    }

    /** The nickname as written by the player. */
    public String value() {
        return value;
    }

    /** Case-insensitive form, used for storage and lookups. */
    public String key() {
        return key;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Nickname nickname && key.equals(nickname.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return value;
    }
}
