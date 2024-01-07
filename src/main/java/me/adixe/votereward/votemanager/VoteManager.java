package me.adixe.votereward.votemanager;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.configuration.Configuration;
import me.adixe.votereward.placeholders.PlaceholdersManager;
import me.adixe.votereward.placeholders.providers.ServerProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class VoteManager {
    private final VoteReward plugin;

    public VoteManager(VoteReward plugin) {
        this.plugin = plugin;
    }

    public boolean rewardPlayer(Player player, String serverKey) {
        Configuration configuration = plugin.getConfiguration();

        YamlFile settings = configuration.get("settings");
        YamlFile data = configuration.get("data");

        String serverPath = "Servers." + serverKey;

        String serverUuid = settings.getString(serverPath + ".Uuid");

        String playerPath = "Votes." + player.getUniqueId();

        List<String> alreadyVotedServers = new ArrayList<>(data.getStringList(playerPath));

        if (data.contains(playerPath) &&
                alreadyVotedServers.contains(serverUuid)) {
            return false;
        }

        alreadyVotedServers.add(serverUuid);

        data.set(playerPath, alreadyVotedServers);

        BukkitRunnable rewardCommandsTask = new BukkitRunnable() {
            @Override
            public void run() {
                PlaceholdersManager placeholdersManager = plugin.getPlaceholdersManager();

                List<String> commands = settings.getStringList(serverPath + ".RewardCommands");

                Map<String, String> placeholders = new HashMap<>();
                placeholders.putAll(placeholdersManager.get(Player.class).getUnique(player));
                placeholders.putAll(placeholdersManager.get(ServerProvider.ServerIdentifier.class)
                        .getUnique(new ServerProvider.ServerIdentifier(serverKey)));

                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        PlaceholdersManager.translate(command, placeholders)));
            }
        };

        rewardCommandsTask.runTask(plugin);

        return true;
    }

    public void autoSave() {
        BukkitRunnable saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                YamlFile data = plugin.getConfiguration().get("data");

                LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

                if (!data.contains("Date") || !LocalDate.parse(data.getString("Date"),
                        DateTimeFormatter.ISO_LOCAL_DATE).isEqual(timeNow)) {
                    data.set("Date", timeNow.format(DateTimeFormatter.ISO_LOCAL_DATE));

                    data.set("Votes", null);
                }

                save();
            }
        };

        saveTask.runTaskTimerAsynchronously(plugin, 0L, 20L * 60 * 10);
    }

    public void save() {
        try {
            plugin.getConfiguration().get("data").save();
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE,
                    "An error occurred while trying to save data.", exception);
        }
    }
}
