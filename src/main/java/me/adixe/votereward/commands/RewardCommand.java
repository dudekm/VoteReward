package me.adixe.votereward.commands;

import me.adixe.commonutilslib.commands.CommandException;
import me.adixe.commonutilslib.commands.executors.CommandExecutor;
import me.adixe.commonutilslib.configuration.SectionContainer;
import me.adixe.commonutilslib.placeholders.PlaceholdersManager;
import me.adixe.commonutilslib.placeholders.providers.PlaceholdersProvider;
import me.adixe.votereward.VoteReward;
import me.adixe.votereward.votemanager.Server;
import me.adixe.votereward.votemanager.VerificationListener;
import me.adixe.votereward.votemanager.VoteManager;
import me.adixe.votereward.votemanager.VoteVerifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RewardCommand extends CommandExecutor {
    public RewardCommand(String permission, SectionContainer settingsContainer) {
        super("reward",
                Collections.emptyList(),
                0, permission, settingsContainer);
    }

    @Override
    protected void perform(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        if (!(sender instanceof Player)) {
            throw new CommandException("only-player");
        }

        Player player = (Player) sender;

        VoteReward plugin = VoteReward.getInstance();

        YamlFile settings = plugin.getConfiguration().get("settings");

        PlaceholdersManager placeholdersManager = plugin.getPlaceholdersManager();

        Map<String, String> placeholders = new HashMap<>(
                placeholdersManager.get(Player.class).getUnique(player));

        sendMessage(sender, "header", placeholders);

        VoteManager voteManager = plugin.getVoteManager();

        PlaceholdersProvider<Server> serverProvider = placeholdersManager.get(Server.class);

        for (String serverKey : settings.getConfigurationSection("servers").getKeys(false)) {
            Server server = new Server(serverKey);

            Map<String, String> serverPlaceholders = new HashMap<>(placeholders);
            serverPlaceholders.putAll(serverProvider.getUnique(server));

            VoteVerifier verifier = new VoteVerifier(server, sender.getName());

            verifier.verify(new VerificationListener() {
                @Override
                public void success() {
                    if (voteManager.rewardPlayer(player, server))
                        sendMessage(sender, "success", serverPlaceholders);
                    else
                        sendMessage(sender, "already-voted", serverPlaceholders);
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
