package me.adixe.votereward.vote;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A single vote returned by the vote list API.
 *
 * @param id       the vote's UUID on the list, unique across all votes
 * @param nickname the player name given when voting; votes cast without one cannot be rewarded
 * @param castAt   when the vote was cast
 */
public record Vote(String id, Optional<Nickname> nickname, Instant castAt) {
    public Vote {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vote ID is empty.");
        }

        Objects.requireNonNull(nickname, "nickname");
        Objects.requireNonNull(castAt, "castAt");
    }

    public static Vote of(String id, String nickname, Instant castAt) {
        Optional<Nickname> parsed = nickname == null || nickname.isBlank()
                ? Optional.empty()
                : Optional.of(Nickname.of(nickname));

        return new Vote(id, parsed, castAt);
    }
}
