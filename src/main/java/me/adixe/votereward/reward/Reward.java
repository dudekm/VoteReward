package me.adixe.votereward.reward;

import me.adixe.votereward.ledger.PendingReward;
import me.adixe.votereward.site.VoteSite;

import java.util.Objects;

/**
 * A pending reward together with the list entry whose commands it runs.
 */
public record Reward(PendingReward pending, VoteSite site) {
    public Reward {
        Objects.requireNonNull(pending, "pending");
        Objects.requireNonNull(site, "site");
    }
}
