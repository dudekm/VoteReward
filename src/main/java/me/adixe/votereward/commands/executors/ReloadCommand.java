package me.adixe.votereward.commands.executors;

import me.adixe.votereward.utils.Configuration;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Map;

public class ReloadCommand extends CommandExecutor {
    public ReloadCommand() {
        super("Reload",
                Collections.emptyList(),
                0, "votereward.reload");
    }

    @Override
    protected void perform(CommandSender sender, Map<String, Object> argsValues) {
        Configuration.load();

        sendMessage(sender, "Success");
    }
}
