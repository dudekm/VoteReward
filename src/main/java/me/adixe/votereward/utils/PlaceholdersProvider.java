package me.adixe.votereward.utils;

import me.adixe.votereward.VoteReward;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlaceholdersProvider {
    public String translate(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet())
            text = text.replace("%" + entry.getKey() + "%", entry.getValue());

        return text;
    }

    public Map<String, String> getPlayerDefault(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player_name", player.getName());
        placeholders.put("player_display_name", player.getDisplayName());
        placeholders.put("player_uuid", player.getUniqueId().toString());

        return placeholders;
    }

    public Map<String, String> getCommandSenderDefault(CommandSender sender) {
        return Collections.singletonMap("sender", sender.getName());
    }

    public Map<String, String> getServerDefault(String server) {
        YamlFile settings = VoteReward.getInstance().getConfiguration().get("settings");

        String serverSettingsPath = "Servers." + server;

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("server_name", server);
        placeholders.put("server_address", settings.getString(serverSettingsPath + ".Address"));
        placeholders.put("server_uuid", settings.getString(serverSettingsPath + ".Uuid"));

        return placeholders;
    }
}
