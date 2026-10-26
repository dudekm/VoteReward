package me.adixe.votereward.site;

import me.adixe.votereward.vote.ServerId;

import java.util.List;
import java.util.Objects;

/**
 * One configured vote list entry: where the server is listed and what voting there earns.
 *
 * @param name           unique key from the configuration, e.g. {@code lista-minecraft}
 * @param address        base address of the list
 * @param serverId       the server's ID on that list
 * @param rewardCommands commands run for every rewarded vote
 */
public record VoteSite(String name, ListAddress address, ServerId serverId, List<RewardCommand> rewardCommands) {
    public VoteSite {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Site name is empty.");
        }

        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(serverId, "serverId");
        rewardCommands = List.copyOf(rewardCommands);
    }
}
