package me.adixe.votereward.commands.args;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class CommandArg {
    protected final VoteReward plugin;
    private final String identifier;

    public CommandArg(VoteReward plugin, String identifier) {
        this.plugin = plugin;
        this.identifier = identifier;
    }

    public abstract Object buildValue(CommandSender sender, String input) throws CommandException;

    public abstract List<String> tabComplete(CommandSender sender);

    public String getIdentifier() {
        return identifier;
    }
}
