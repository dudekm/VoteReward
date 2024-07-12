package me.adixe.votereward.commands;

import me.adixe.commonutilslib.command.CommandException;
import me.adixe.commonutilslib.command.CommandExecutor;
import me.adixe.commonutilslib.configuration.SectionContainer;
import me.adixe.commonutilslib.placeholder.PlaceholderManager;
import me.adixe.votereward.VoteReward;
import me.adixe.votereward.votemanager.Server;
import me.adixe.votereward.votemanager.VerificationListener;
import me.adixe.votereward.votemanager.VoteManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RewardCommand extends CommandExecutor {
    public RewardCommand(String name, String permission,
                         SectionContainer settingsContainer) {
        super(name, permission, settingsContainer, List.of(), 0);
    }

    @Override
    protected void execute(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        if (!(sender instanceof Player player)) {
            throw new CommandException("only-player");
        }

        VoteReward plugin = VoteReward.getInstance();

        YamlFile settings = plugin.getConfiguration().get("settings");

        PlaceholderManager placeholderManager = plugin.getPlaceholderManager();

        Map<String, String> placeholders = new HashMap<>(
                placeholderManager.get(Player.class).getUnique(player));

        sendMessage(sender, "header", placeholders);

        for (String serverKey : settings.getConfigurationSection("servers").getKeys(false)) {
            VoteManager voteManager = plugin.getVoteManager();

            Server server = new Server(serverKey);

            Map<String, String> serverPlaceholders = new HashMap<>(placeholders);
            serverPlaceholders.putAll(placeholderManager.get(Server.class).getUnique(server));

            voteManager.verifyAsync(server, player, new VerificationListener() {
                @Override
                public void success() {
                    if (voteManager.rewardPlayer(player, server)) {
                        sendMessage(sender, "success", serverPlaceholders);
                    } else {
                        sendMessage(sender, "already-voted", serverPlaceholders);
                    }
                }

                @Override
                public void notFound() {
                    sendMessage(sender, "not-found", serverPlaceholders);
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
