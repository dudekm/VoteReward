package me.adixe.votereward.bukkit;

import me.adixe.votereward.text.Placeholders;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Texts from {@code messages.yml}, coloured with {@code &} codes. Keys missing from the
 * server's file fall back to the defaults shipped in the jar, so an old file keeps working
 * after an update that adds messages.
 */
public final class Messages {
    private final YamlConfiguration file;

    public Messages(YamlConfiguration file) {
        this.file = file;
    }

    public String get(String key, Placeholders placeholders) {
        String text = file.getString(key);

        if (text == null) {
            text = key;
        }

        return ChatColor.translateAlternateColorCodes('&', placeholders.apply(text));
    }

    public void send(CommandSender receiver, String key) {
        send(receiver, key, Placeholders.empty());
    }

    public void send(CommandSender receiver, String key, Placeholders placeholders) {
        String text = get(key, placeholders);

        // An empty message in messages.yml switches it off.
        if (!text.isEmpty()) {
            receiver.sendMessage(text);
        }
    }
}
