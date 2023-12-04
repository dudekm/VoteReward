package me.adixe.votereward.utils;

import me.adixe.votereward.VoteReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
    public static boolean rewardPlayer(Player player, String server) {
        YamlFile settings = Configuration.get("settings");
        YamlFile data = Configuration.get("data");

        String serverSettingsPath = "Servers." + server;

        String serverUuid = settings.getString(serverSettingsPath + ".Uuid");

        String playerSettingsPath = "Votes." + player.getUniqueId();

        List<String> alreadyVotedServers = new ArrayList<>(data.getStringList(playerSettingsPath));

        if (data.contains(playerSettingsPath) &&
                alreadyVotedServers.contains(serverUuid))
            return false;

        alreadyVotedServers.add(serverUuid);

        data.set(playerSettingsPath, alreadyVotedServers);

        Bukkit.getScheduler().runTask(VoteReward.getInstance(), () -> {
            List<String> commands = settings.getStringList(serverSettingsPath + ".RewardCommands");

            Map<String, String> placeholders = new HashMap<>();
            placeholders.putAll(PlaceholdersProvider.getPlayerDefault(player));
            placeholders.putAll(PlaceholdersProvider.getServerDefault(server));

            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    PlaceholdersProvider.translate(command, placeholders)));
        });

        return true;
    }

    public static void setup() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(VoteReward.getInstance(), () -> {
            YamlFile data = Configuration.get("data");

            LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

            if (!data.contains("Date") || !LocalDate.parse(data.getString("Date"),
                    DateTimeFormatter.ISO_LOCAL_DATE).isEqual(timeNow)) {
                data.set("Date", timeNow.format(DateTimeFormatter.ISO_LOCAL_DATE));

                data.set("Votes", null);
            }

            saveData();
        }, 0L, 20L * 60 * 10);
    }

    public static void saveData() {
        try {
            Configuration.get("data").save();
        } catch (IOException exception) {
            VoteReward.getInstance().getLogger().log(Level.SEVERE,
                    "An error occurred while trying to save data.", exception);
        }
    }
}
