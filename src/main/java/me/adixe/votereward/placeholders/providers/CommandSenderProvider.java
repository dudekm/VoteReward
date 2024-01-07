package me.adixe.votereward.placeholders.providers;

import me.adixe.votereward.VoteReward;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Map;

public class CommandSenderProvider extends PlaceholdersProvider<CommandSender> {
    public CommandSenderProvider(VoteReward plugin) {
        super(plugin, CommandSender.class, "sender");
    }

    @Override
    public Map<String, String> get(CommandSender sender) {
        return Collections.singletonMap("name", sender.getName());
    }
}
