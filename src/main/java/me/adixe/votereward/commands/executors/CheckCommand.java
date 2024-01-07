package me.adixe.votereward.commands.executors;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.CommandException;
import me.adixe.votereward.commands.args.PlayerArg;
import me.adixe.votereward.placeholders.PlaceholdersManager;
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

public class CheckCommand extends CommandExecutor {
    public CheckCommand(VoteReward plugin) {
        super(plugin, "Check",
                Collections.singletonList(new PlayerArg(plugin, "player")),
                0, "votereward.check");
    }

    @Override
    protected void perform(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        Player player;

        if (argsValues.containsKey("player")) {
            player = (Player) argsValues.get("player");
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            throw new CommandException("NoPlayerGiven");
        }

        PlaceholdersManager placeholdersManager = plugin.getPlaceholdersManager();

        Map<String, String> placeholders = placeholdersManager.get(Player.class).getUnique(player);

        sendMessage(sender, "Header", placeholders);

        YamlFile settings = plugin.getConfiguration().get("settings");

        for (String serverKey : settings.getConfigurationSection("Servers").getKeys(false)) {
            VoteVerifier verifier = new VoteVerifier(plugin, serverKey, player.getName());

            Map<String, String> serverPlaceholders = new HashMap<>(placeholders);
            serverPlaceholders.putAll(placeholdersManager.get(ServerProvider.ServerIdentifier.class)
                    .getUnique(new ServerProvider.ServerIdentifier(serverKey)));

            verifier.verify(new VerificationListener() {
                @Override
                public void success() {
                    sendMessage(sender, "Success", serverPlaceholders);
                }

                @Override
                public void notFound() {
                    sendMessage(sender, "NotFound", serverPlaceholders);
                }

                @Override
                public void exceptionCaught(IOException exception) {
                    Map<String, String> exceptionPlaceholders = new HashMap<>(serverPlaceholders);
                    exceptionPlaceholders.put("exception", exception.getMessage());

                    sendMessage(sender, "ExceptionCaught", exceptionPlaceholders);

                    plugin.getLogger().log(Level.SEVERE,
                            "An error occurred while checking vote.",
                            exception);
                }
            });
        }
    }
}
