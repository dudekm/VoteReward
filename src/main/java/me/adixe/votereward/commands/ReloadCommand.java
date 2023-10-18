package me.adixe.votereward.commands;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.utils.MessagesUtility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ReloadCommand implements CommandExecutor {
    private final VoteReward instance;

    public ReloadCommand(VoteReward instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String arg, @NotNull String[] args) {
        MessagesUtility messagesUtility = instance.getMessagesUtility();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sender", sender.getName());

        if (!sender.hasPermission("votereward.reload")) {
            messagesUtility.sendMessage(sender,
                    "Commands.Reload.NoPermission",
                    placeholders);

            return false;
        }

        try {
            instance.getConfiguration().get("settings").load();

            messagesUtility.sendMessage(sender,
                    "Commands.Reload.Success",
                    placeholders);

            return true;
        } catch (IOException exception) {
            instance.getLogger().log(Level.SEVERE,
                    "An error occurred while trying to load settings.", exception);

            messagesUtility.sendMessage(sender,
                    "Commands.Reload.IOError",
                    placeholders);

            return false;
        }
    }
}
