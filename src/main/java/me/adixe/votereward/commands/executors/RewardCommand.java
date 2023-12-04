package me.adixe.votereward.commands.executors;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.CommandException;
import me.adixe.votereward.utils.Configuration;
import me.adixe.votereward.utils.PlaceholdersProvider;
import me.adixe.votereward.utils.VoteManager;
import me.adixe.votereward.voteverifier.VerificationListener;
import me.adixe.votereward.voteverifier.VoteVerifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RewardCommand extends CommandExecutor {
    public RewardCommand() {
        super("Reward",
                Collections.emptyList(),
                0, "votereward.reward");
    }

    @Override
    protected void perform(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        if (!(sender instanceof Player))
            throw new CommandException("OnlyPlayer");

        sendMessage(sender, "Header");

        YamlFile settings = Configuration.get("settings");

        for (String server : settings.getConfigurationSection("Servers").getKeys(false)) {
            VoteVerifier verifier = new VoteVerifier(server, sender.getName());

            Map<String, String> serverPlaceholders = PlaceholdersProvider.getServerDefault(server);

            verifier.verify(new VerificationListener() {
                @Override
                public void success() {
                    if (VoteManager.rewardPlayer((Player) sender, server))
                        sendMessage(sender, "Success", serverPlaceholders);
                    else
                        sendMessage(sender, "AlreadyVoted", serverPlaceholders);
                }

                @Override
                public void notFound() {
                    sendMessage(sender, "NotFound", serverPlaceholders);
                }

                @Override
                public void exceptionCaught(Exception exception) {
                    Map<String, String> exceptionPlaceholders = new HashMap<>(serverPlaceholders);
                    exceptionPlaceholders.put("exception", exception.getMessage());

                    sendMessage(sender, "ExceptionCaught", exceptionPlaceholders);

                    VoteReward.getInstance().getLogger().log(Level.SEVERE,
                            "An error occurred while awarding reward.",
                            exception);
                }
            });
        }
    }
}
