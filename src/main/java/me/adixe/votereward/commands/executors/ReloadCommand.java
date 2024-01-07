package me.adixe.votereward.commands.executors;

import me.adixe.votereward.VoteReward;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Map;

public class ReloadCommand extends CommandExecutor {
    public ReloadCommand(VoteReward plugin) {
        super(plugin, "Reload",
                Collections.emptyList(),
                0, "votereward.reload");
    }

    @Override
    protected void perform(CommandSender sender, Map<String, Object> argsValues) {
        plugin.getConfiguration().reload();

        sendMessage(sender, "Success");
    }
}
