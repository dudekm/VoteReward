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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class VoteManager {
    public boolean rewardPlayer(Player player, String server) {
        VoteReward voteReward = VoteReward.getInstance();

        Configuration configuration = voteReward.getConfiguration();

        YamlFile settings = configuration.get("settings");
        YamlFile data = configuration.get("data");

        String serverSettingsPath = "Servers." + server;

        String serverUuid = settings.getString(serverSettingsPath + ".Uuid");

        String playerSettingsPath = "Votes." + player.getUniqueId();

        List<String> alreadyVotedServers = new ArrayList<>(data.getStringList(playerSettingsPath));

        if (data.contains(playerSettingsPath) &&
                alreadyVotedServers.contains(serverUuid))
            return false;

        alreadyVotedServers.add(serverUuid);

        data.set(playerSettingsPath, alreadyVotedServers);

        Bukkit.getScheduler().runTask(voteReward, () -> {
            List<String> commands = settings.getStringList(serverSettingsPath + ".RewardCommands");

            PlaceholdersProvider placeholdersProvider = voteReward.getPlaceholdersProvider();

            Map<String, String> placeholders = placeholdersProvider.getPlayerDefault(player);

            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    placeholdersProvider.translate(command, placeholders)));
        });

        return true;
    }

    public void setup() {
        VoteReward voteReward = VoteReward.getInstance();

        Bukkit.getScheduler().runTaskTimerAsynchronously(voteReward, () -> {
            YamlFile data = voteReward.getConfiguration().get("data");

            LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

            if (!data.contains("Date") || !LocalDate.parse(data.getString("Date"),
                    DateTimeFormatter.ISO_LOCAL_DATE).isEqual(timeNow)) {
                data.set("Date", timeNow.format(DateTimeFormatter.ISO_LOCAL_DATE));

                data.set("Votes", null);
            }

            saveData();
        }, 0L, 20L * 60 * 10);
    }

    public void saveData() {
        VoteReward voteReward = VoteReward.getInstance();

        try {
            voteReward.getConfiguration().get("data").save();
        } catch (IOException exception) {
            voteReward.getLogger().log(Level.SEVERE,
                    "An error occurred while trying to save data.", exception);
        }
    }
}
