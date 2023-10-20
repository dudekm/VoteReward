package me.adixe.votereward.commands;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.utils.Configuration;
import me.adixe.votereward.utils.MessagesUtility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RewardCommand implements CommandExecutor {
    private final VoteReward instance;

    public RewardCommand(VoteReward instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String arg, @NotNull String[] args) {
        MessagesUtility messagesUtility = instance.getMessagesUtility();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sender", sender.getName());

        if (!sender.hasPermission("votereward.reward")) {
            messagesUtility.sendMessage(sender,
                    "Commands.Reward.NoPermission",
                    placeholders);

            return false;
        }

        if (sender instanceof Player player) {
            messagesUtility.sendMessage(sender,
                    "Commands.Reward.Header",
                    placeholders);

            BukkitScheduler scheduler = instance.getServer().getScheduler();

            scheduler.runTaskAsynchronously(instance, () -> {
                Configuration configuration = instance.getConfiguration();

                YamlFile settings = configuration.get("settings");
                YamlFile data = configuration.get("data");

                String playerSettingsPath = "Votes." + player.getUniqueId();

                for (String key : settings.getConfigurationSection("Servers").getKeys(false)) {
                    String serverSettingsPath = "Servers." + key;

                    String serverAddress = settings.getString(serverSettingsPath + ".Address");
                    String serverUuid = settings.getString(serverSettingsPath + ".Uuid");

                    Map<String, String> serverPlaceholders = new HashMap<>(placeholders);
                    serverPlaceholders.put("server_name", key);
                    serverPlaceholders.put("server_address", serverAddress);
                    serverPlaceholders.put("server_uuid", serverUuid);

                    if (!data.contains(playerSettingsPath) ||
                            !data.getStringList(playerSettingsPath).contains(serverUuid)) {
                        try {
                            InputStream inputStream = new URL(serverAddress + "/votes/server/" + serverUuid).openStream();

                            JSONArray json = new JSONArray(new BufferedReader(
                                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)).readLine());

                            boolean voted = false;

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject entry = json.getJSONObject(i);

                                if (entry.isNull("nickname"))
                                    continue;

                                String nickname = entry.getString("nickname");
                                LocalDate createdAt = LocalDate.parse(entry.getString("createdAt"),
                                        DateTimeFormatter.ISO_DATE_TIME);

                                if (nickname.equals(player.getName()) &&
                                        createdAt.isEqual(LocalDate.now(ZoneId.of("Europe/Paris")))) {
                                    voted = true;

                                    break;
                                }
                            }

                            inputStream.close();

                            if (voted) {
                                List<String> votedServers = new ArrayList<>();

                                if (data.contains(playerSettingsPath))
                                    votedServers.addAll(data.getStringList(playerSettingsPath));

                                votedServers.add(serverUuid);

                                data.set(playerSettingsPath, votedServers);

                                scheduler.runTask(instance, () ->
                                        settings.getStringList(serverSettingsPath + ".RewardCommands")
                                                .forEach(rewardCommand -> Bukkit.dispatchCommand(
                                                        Bukkit.getConsoleSender(),
                                                        messagesUtility.includePlaceholders(
                                                                rewardCommand, serverPlaceholders))));

                                messagesUtility.sendMessage(sender,
                                        "Commands.Reward.Success",
                                        serverPlaceholders);
                            } else
                                messagesUtility.sendMessage(sender,
                                        "Commands.Reward.NotFound",
                                        serverPlaceholders);
                        } catch (IOException exception) {
                            instance.getLogger().log(Level.SEVERE,
                                    "An error occurred while trying to check vote.", exception);

                            messagesUtility.sendMessage(sender,
                                    "Commands.Reward.IOError",
                                    serverPlaceholders);
                        }
                    } else
                        messagesUtility.sendMessage(sender,
                                "Commands.Reward.AlreadyVoted",
                                serverPlaceholders);
                }
            });

            return true;
        } else {
            messagesUtility.sendMessage(sender,
                    "Commands.Reward.OnlyPlayer",
                    placeholders);

            return false;
        }
    }
}
