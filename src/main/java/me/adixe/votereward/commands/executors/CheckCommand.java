package me.adixe.votereward.commands.executors;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.CommandException;
import me.adixe.votereward.commands.args.PlayerArg;
import me.adixe.votereward.utils.PlaceholdersProvider;
import me.adixe.votereward.voteverifier.VerificationListener;
import me.adixe.votereward.voteverifier.VoteVerifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CheckCommand extends CommandExecutor {
    public CheckCommand() {
        super("Check",
                Collections.singletonList(new PlayerArg("player")),
                0, "votereward.check");
    }

    @Override
    protected void perform(CommandSender sender, Map<String, Object> argsValues) throws CommandException {
        Player player;

        if (argsValues.containsKey("player"))
            player = (Player) argsValues.get("player");
        else if (sender instanceof Player)
            player = (Player) sender;
        else
            throw new CommandException("NoPlayerGiven");

        VoteReward voteReward = VoteReward.getInstance();

        PlaceholdersProvider placeholdersProvider = voteReward.getPlaceholdersProvider();

        Map<String, String> placeholders = placeholdersProvider.getPlayerDefault(player);

        sendMessage(sender, "Header", placeholders);

        YamlFile settings = voteReward.getConfiguration().get("settings");

        for (String server : settings.getConfigurationSection("Servers").getKeys(false)) {
            VoteVerifier verifier = new VoteVerifier(server, player.getName());

            Map<String, String> serverPlaceholders = new HashMap<>(placeholders);
            serverPlaceholders.putAll(placeholdersProvider.getServerDefault(server));

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
                public void exceptionCaught(Exception exception) {
                    Map<String, String> exceptionPlaceholders = new HashMap<>(serverPlaceholders);
                    exceptionPlaceholders.put("exception", exception.getMessage());

                    sendMessage(sender, "ExceptionCaught", exceptionPlaceholders);

                    voteReward.getLogger().log(Level.SEVERE,
                            "An error occurred while checking vote.",
                            exception);
                }
            });
        }
    }
}
