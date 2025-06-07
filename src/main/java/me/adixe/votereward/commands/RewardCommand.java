package me.adixe.votereward.commands;

import me.adixe.commonutilslib.command.CommandException;
import me.adixe.commonutilslib.command.CommandExecutor;
import me.adixe.commonutilslib.configuration.Configuration;
import me.adixe.commonutilslib.configuration.SectionHolder;
import me.adixe.commonutilslib.placeholder.PlaceholderManager;
import me.adixe.votereward.VoteReward;
import me.adixe.votereward.vote.ServerHolder;
import me.adixe.votereward.vote.VerificationListener;
import me.adixe.votereward.vote.VoteManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RewardCommand extends CommandExecutor {
    public RewardCommand(String name, String permission, SectionHolder messagesHolder) {
        super(name, permission, messagesHolder, List.of(), 0);
    }

    @Override
    protected void execute(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        if (!(sender instanceof Player player)) {
            throw new CommandException("only-player");
        }

        VoteReward plugin = VoteReward.getInstance();

        PlaceholderManager placeholderManager = plugin.getPlaceholderManager();

        Map<String, String> placeholders = new HashMap<>(
                placeholderManager.get(Player.class).getUnique(player));

        sendMessage(sender, "process-started", placeholders);

        for (ConfigurationSection serverSettings : Configuration.getSections(
                plugin.getConfiguration().get("settings"), "servers")) {
            VoteManager voteManager = plugin.getVoteManager();

            ServerHolder server = new ServerHolder(serverSettings);

            Map<String, String> serverPlaceholders = new HashMap<>(placeholders);
            serverPlaceholders.putAll(placeholderManager.get(ServerHolder.class).getUnique(server));

            voteManager.verifyAsync(player, server, new VerificationListener() {
                @Override
                public void success() {
                    voteManager.rewardPlayer(player, server);

                    sendMessage(sender, "success", serverPlaceholders);
                }

                @Override
                public void notFound() {
                    sendMessage(sender, "not-found", serverPlaceholders);
                }

                @Override
                public void rewardGranted() {
                    sendMessage(sender, "already-awarded", serverPlaceholders);
                }

                @Override
                public void exceptionCaught(Exception exception) {
                    Map<String, String> exceptionPlaceholders = new HashMap<>(serverPlaceholders);
                    exceptionPlaceholders.put("exception", exception.getMessage());

                    sendMessage(sender, "exception-caught", exceptionPlaceholders);

                    plugin.getLogger().log(Level.SEVERE,
                            "An error occurred while awarding reward.",
                            exception);
                }
            });
        }
    }
}
