package me.adixe.votereward.commands;

import me.adixe.commonutilslib.command.CommandException;
import me.adixe.commonutilslib.command.CommandExecutor;
import me.adixe.commonutilslib.command.arg.PlayerArg;
import me.adixe.commonutilslib.configuration.Configuration;
import me.adixe.commonutilslib.configuration.SectionHolder;
import me.adixe.commonutilslib.placeholder.PlaceholderManager;
import me.adixe.votereward.VoteReward;
import me.adixe.votereward.vote.ServerHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckCommand extends CommandExecutor {
    public CheckCommand(String name, String permission, SectionHolder messagesHolder) {
        super(name, permission, messagesHolder,
                List.of(new PlayerArg("player")), 0);
    }

    @Override
    protected void execute(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        VoteReward plugin = VoteReward.getInstance();

        YamlFile settings = plugin.getConfiguration().get("settings");

        PlaceholderManager placeholderManager = plugin.getPlaceholderManager();

        if (argsValues.containsKey("player")) {
            Player player = (Player) argsValues.get("player");

            Map<String, String> placeholders = new HashMap<>(
                    placeholderManager.get(Player.class).getUnique(player));

            sendMessage(sender, "header.other", placeholders);

            for (ConfigurationSection serverSettings : Configuration.getSections(settings, "servers")) {
                ServerHolder server = new ServerHolder(serverSettings);

                Map<String, String> serverPlaceholders = new HashMap<>(placeholders);
                serverPlaceholders.putAll(placeholderManager.get(ServerHolder.class).getUnique(server));

                if (plugin.getVoteManager().isRewardGranted(player, server)) {
                    sendMessage(sender, "success.other", serverPlaceholders);
                } else {
                    sendMessage(sender, "not-granted.other", serverPlaceholders);
                }
            }
        } else if (sender instanceof Player player) {
            sendMessage(sender, "header.self");

            for (ConfigurationSection serverSettings : Configuration.getSections(settings, "servers")) {
                ServerHolder server = new ServerHolder(serverSettings);

                Map<String, String> serverPlaceholders = new HashMap<>(
                        placeholderManager.get(ServerHolder.class).getUnique(server));

                if (plugin.getVoteManager().isRewardGranted(player, server)) {
                    sendMessage(sender, "success.self", serverPlaceholders);
                } else {
                    sendMessage(sender, "not-granted.self", serverPlaceholders);
                }
            }
        } else {
            throw new CommandException("no-player-given");
        }
    }
}
