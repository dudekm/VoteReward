package me.adixe.votereward.commands.args;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.commands.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerArg extends CommandArg {
    public PlayerArg(VoteReward plugin, String identifier) {
        super(plugin, identifier);
    }

    @Override
    public Object buildValue(CommandSender sender, String input) throws CommandException {
        Player player = Bukkit.getPlayer(input);

        if (player != null && (!(sender instanceof Player) || ((Player) sender).canSee(player))) {
            return player;
        }

        throw new CommandException("PlayerNotFound",
                Collections.singletonMap("player", input));
    }

    @Override
    public List<String> tabComplete(CommandSender sender) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !(sender instanceof Player) || ((Player) sender).canSee(player))
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
