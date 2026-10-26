package me.adixe.votereward.ledger;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * How far one list has been read.
 *
 * @param position    the next check asks for votes from this moment on
 * @param recentVotes IDs (with cast time) of votes already handled at or after {@code position},
 *                    which the next check will see again and must skip
 */
public record SiteCursor(Instant position, Map<String, Instant> recentVotes) {
    public SiteCursor {
        Objects.requireNonNull(position, "position");
        recentVotes = Map.copyOf(recentVotes);
    }
}
