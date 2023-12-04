package me.adixe.votereward.utils;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class MessagesUtility {
    public static void sendMessage(CommandSender recipient, String settingsPath, Map<String, String> placeholders) {
        String message = Configuration.get("settings").getString(settingsPath);

        if (message == null)
            throw new NullPointerException("No value found for \"" + settingsPath + "\"");

        if (message.isEmpty())
            return;

        recipient.spigot().sendMessage(TextComponent.fromLegacyText(
                ChatColor.translateAlternateColorCodes('&',
                        PlaceholdersProvider.translate(message, placeholders))));
    }
}
