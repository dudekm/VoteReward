package me.adixe.votereward.utils;

import me.adixe.votereward.placeholders.PlaceholdersManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Map;

public class MessagesUtility {
    public static void sendMessage(CommandSender recipient, YamlFile file,
                            String path, Map<String, String> placeholders) {
        if (file.isConfigurationSection(path)) {
            if (file.contains(path + ".Message")) {
                recipient.spigot().sendMessage(translateComponent(
                        file.getString(path + ".Message"), placeholders));
            }

            if (recipient instanceof Player) {
                Player player = (Player) recipient;

                if (file.contains(path + ".Title")) {
                    player.sendTitle(
                            translate(file.getString(path + ".Title.Title"), placeholders),
                            translate(file.getString(path + ".Title.Subtitle"), placeholders),
                            file.getInt(path + ".Title.FadeIn", 0),
                            file.getInt(path + ".Title.Stay", 60),
                            file.getInt(path + ".Title.FadeOut", 10));
                }

                if (file.contains(path + ".ActionBar")) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, translateComponent(
                            file.getString(path + ".ActionBar"), placeholders));
                }
            }
        } else if (file.isString(path)) {
            recipient.spigot().sendMessage(translateComponent(
                    file.getString(path), placeholders));
        } else {
            throw new IllegalArgumentException("Invalid message format for " + path);
        }
    }

    public static String translateColorCodes(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String translate(String text, Map<String, String> placeholders) {
        return translateColorCodes(PlaceholdersManager.translate(text, placeholders));
    }

    public static BaseComponent[] translateComponent(String text, Map<String, String> placeholders) {
        return TextComponent.fromLegacyText(translate(text, placeholders));
    }
}
