package me.adixe.votereward.commands;

import me.adixe.commonutilslib.commands.CommandException;
import me.adixe.commonutilslib.commands.args.PlayerArg;
import me.adixe.commonutilslib.commands.executors.CommandExecutor;
import me.adixe.commonutilslib.configuration.SectionContainer;
import me.adixe.commonutilslib.placeholders.PlaceholdersManager;
import me.adixe.commonutilslib.placeholders.providers.PlaceholdersProvider;
import me.adixe.votereward.VoteReward;
import me.adixe.votereward.votemanager.Server;
import me.adixe.votereward.votemanager.VerificationListener;
import me.adixe.votereward.votemanager.VoteVerifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CheckCommand extends CommandExecutor {
    public CheckCommand(String permission, SectionContainer settingsContainer) {
        super("check",
                Collections.singletonList(new PlayerArg("player")),
                0, permission, settingsContainer);
    }

    @Override
    protected void perform(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        VoteReward plugin = VoteReward.getInstance();

        YamlFile settings = plugin.getConfiguration().get("settings");

        PlaceholdersManager placeholdersManager = plugin.getPlaceholdersManager();

        PlaceholdersProvider<Server> serverProvider = placeholdersManager.get(Server.class);

        if (argsValues.containsKey("player")) {
            Player player = (Player) argsValues.get("player");

            Map<String, String> placeholders = new HashMap<>(
                    placeholdersManager.get(Player.class).getUnique(player));

            sendMessage(sender, "process-started.other", placeholders);

            for (String serverKey : settings.getConfigurationSection("servers").getKeys(false)) {
                Server server = new Server(serverKey);

                Map<String, String> serverPlaceholders = new HashMap<>(placeholders);
                serverPlaceholders.putAll(serverProvider.getUnique(server));

                VoteVerifier verifier = new VoteVerifier(server, player.getName());

                verifier.verify(new VerificationListener() {
                    @Override
                    public void success() {
                        sendMessage(sender, "success.other", serverPlaceholders);
                    }

                    @Override
                    public void notFound() {
                        sendMessage(sender, "not-found.other", serverPlaceholders);
                    }

                    @Override
                    public void exceptionCaught(Exception exception) {
                        Map<String, String> exceptionPlaceholders = new HashMap<>(serverPlaceholders);
                        exceptionPlaceholders.put("exception", exception.getMessage());

                        sendMessage(sender, "exception-caught.other", exceptionPlaceholders);

                        plugin.getLogger().log(Level.SEVERE,
                                "An error occurred while checking vote.",
                                exception);
                    }
                });
            }
        } else if (sender instanceof Player) {
            sendMessage(sender, "process-started.self");

            for (String serverKey : settings.getConfigurationSection("servers").getKeys(false)) {
                Server server = new Server(serverKey);

                Map<String, String> serverPlaceholders = new HashMap<>(
                        serverProvider.getUnique(server));

                VoteVerifier verifier = new VoteVerifier(server, sender.getName());

                verifier.verify(new VerificationListener() {
                    @Override
                    public void success() {
                        sendMessage(sender, "success.self", serverPlaceholders);
                    }

                    @Override
                    public void notFound() {
                        sendMessage(sender, "not-found.self", serverPlaceholders);
                    }

                    @Override
                    public void exceptionCaught(Exception exception) {
                        Map<String, String> exceptionPlaceholders = new HashMap<>(serverPlaceholders);
                        exceptionPlaceholders.put("exception", exception.getMessage());

                        sendMessage(sender, "exception-caught.self", exceptionPlaceholders);

                        plugin.getLogger().log(Level.SEVERE,
                                "An error occurred while checking vote.",
                                exception);
                    }
                });
            }
        } else {
            throw new CommandException("no-player-given");
        }
    }
}
