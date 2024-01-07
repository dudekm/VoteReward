package me.adixe.votereward.placeholders.providers;

import me.adixe.votereward.VoteReward;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerProvider extends PlaceholdersProvider<Player> {
    public PlayerProvider(VoteReward plugin) {
        super(plugin, Player.class, "player");
    }

    @Override
    public Map<String, String> get(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", player.getName());
        placeholders.put("display_name", player.getDisplayName());
        placeholders.put("uuid", player.getUniqueId().toString());

        return placeholders;
    }
}
