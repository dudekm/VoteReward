package me.adixe.votereward.commands;

import me.adixe.commonutilslib.command.CommandExecutor;
import me.adixe.commonutilslib.configuration.SectionContainer;
import me.adixe.votereward.VoteReward;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ReloadCommand extends CommandExecutor {
    public ReloadCommand(String name, String permission,
                         SectionContainer settingsContainer) {
        super(name, permission, settingsContainer, List.of(), 0);
    }

    @Override
    protected void execute(CommandSender sender, Map<String, Object> argsValues) {
        VoteReward plugin = VoteReward.getInstance();

        try {
            plugin.getConfiguration().reload();
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE,
                    "An error occurred while loading configuration.",
                    exception);
        }

        sendMessage(sender, "success");
    }
}
