package me.adixe.votereward.bukkit;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.vote.Nickname;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Gives a joining player the rewards for votes cast while they were offline.
 */
public final class PlayerJoinListener implements Listener {
    private final VoteReward plugin;

    public PlayerJoinListener(VoteReward plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.deliver(Nickname.of(event.getPlayer().getName()));
    }
}
