package me.adixe.votereward.votemanager;

import me.adixe.commonutilslib.placeholders.PlaceholdersManager;
import me.adixe.votereward.VoteReward;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.simpleyaml.configuration.file.YamlFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteManager {
    public VoteManager() {
        VoteReward plugin = VoteReward.getInstance();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            YamlFile votes = plugin.getConfiguration().get("votes");

            LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

            if (!votes.contains("date") || !LocalDate.parse(votes.getString("date"),
                    DateTimeFormatter.ISO_LOCAL_DATE).isEqual(timeNow)) {
                votes.set("date", timeNow.format(DateTimeFormatter.ISO_LOCAL_DATE));

                votes.set("votes", null);
            }
        }, 0L, 20L * 60 * 10);
    }

    public boolean rewardPlayer(Player player, Server server) {
        VoteReward plugin = VoteReward.getInstance();

        YamlFile votes = plugin.getConfiguration().get("votes");

        String playerPath = "votes." + player.getUniqueId();

        List<String> alreadyVotedServers = new ArrayList<>(votes.getStringList(playerPath));

        String serverUuid = server.getUuid();

        if (votes.contains(playerPath) &&
                alreadyVotedServers.contains(serverUuid)) {
            return false;
        }

        alreadyVotedServers.add(serverUuid);

        votes.set(playerPath, alreadyVotedServers);

        Bukkit.getScheduler().runTask(plugin, () -> {
            PlaceholdersManager placeholdersManager = plugin.getPlaceholdersManager();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.putAll(placeholdersManager.get(Player.class).getUnique(player));
            placeholders.putAll(placeholdersManager.get(Server.class).getUnique(server));

            CommandSender sender = Bukkit.getConsoleSender();

            for (String command : server.getRewardCommands()) {
                Bukkit.dispatchCommand(sender, PlaceholdersManager.translate(command, placeholders));
            }
        });

        return true;
    }
}
