package me.adixe.votereward.commands.executors;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.CommandException;
import me.adixe.votereward.placeholders.providers.ServerProvider;
import me.adixe.votereward.votemanager.VerificationListener;
import me.adixe.votereward.votemanager.VoteVerifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RewardCommand extends CommandExecutor {
    public RewardCommand(VoteReward plugin) {
        super(plugin, "Reward",
                Collections.emptyList(),
                0, "votereward.reward");
    }

    @Override
    protected void perform(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        if (!(sender instanceof Player)) {
            throw new CommandException("OnlyPlayer");
        }

        sendMessage(sender, "Header");

        YamlFile settings = plugin.getConfiguration().get("settings");

        for (String serverKey : settings.getConfigurationSection("Servers").getKeys(false)) {
            VoteVerifier verifier = new VoteVerifier(plugin, serverKey, sender.getName());

            Map<String, String> placeholders = new HashMap<>(
                    plugin.getPlaceholdersManager().get(ServerProvider.ServerIdentifier.class)
                            .getUnique(new ServerProvider.ServerIdentifier(serverKey)));

            verifier.verify(new VerificationListener() {
                @Override
                public void success() {
                    if (plugin.getVoteManager().rewardPlayer((Player) sender, serverKey)) {
                        sendMessage(sender, "Success", placeholders);
                    } else {
                        sendMessage(sender, "AlreadyVoted", placeholders);
                    }
                }

                @Override
                public void notFound() {
                    sendMessage(sender, "NotFound", placeholders);
                }

                @Override
                public void exceptionCaught(IOException exception) {
                    Map<String, String> exceptionPlaceholders = new HashMap<>(placeholders);
                    exceptionPlaceholders.put("exception", exception.getMessage());

                    sendMessage(sender, "ExceptionCaught", exceptionPlaceholders);

                    plugin.getLogger().log(Level.SEVERE,
                            "An error occurred while awarding reward.",
                            exception);
                }
            });
        }
    }
}
