package me.adixe.votereward.ledger;

import me.adixe.votereward.vote.Nickname;

import java.time.Instant;
import java.util.Objects;

/**
 * A reward earned by a vote but not yet given to the player.
 *
 * @param voteId   the vote that earned it
 * @param site     name of the configured list the vote was cast on
 * @param nickname who voted
 * @param castAt   when the vote was cast
 */
public record PendingReward(String voteId, String site, Nickname nickname, Instant castAt) {
    public PendingReward {
        Objects.requireNonNull(voteId, "voteId");
        Objects.requireNonNull(site, "site");
        Objects.requireNonNull(nickname, "nickname");
        Objects.requireNonNull(castAt, "castAt");
    }
}
