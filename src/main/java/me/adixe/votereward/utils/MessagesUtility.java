package me.adixe.votereward.utils;

import me.adixe.votereward.VoteReward;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class MessagesUtility {
    private final VoteReward instance;

    public MessagesUtility(VoteReward instance) {
        this.instance = instance;
    }

    public void sendMessage(CommandSender recipient, String messageSettingsPath, Map<String, String> placeholders) {
        String message = instance.getConfiguration()
                .get("settings").getString(messageSettingsPath);

        if (message.isEmpty())
            return;

        recipient.spigot().sendMessage(TextComponent.fromLegacyText(
                ChatColor.translateAlternateColorCodes('&',
                        includePlaceholders(message, placeholders))));
    }

    public String includePlaceholders(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet())
            text = text.replace("%" + entry.getKey() + "%", entry.getValue());

        return text;
    }
}
