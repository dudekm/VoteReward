package me.adixe.votereward.votemanager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adixe.votereward.VoteReward;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class VoteVerifier {
    private final VoteReward plugin;
    private final String serverKey,
            playerName;

    public VoteVerifier(VoteReward plugin, String serverKey, String playerName) {
        this.plugin = plugin;
        this.serverKey = serverKey;
        this.playerName = playerName;
    }

    public void verify(VerificationListener listener) {
        BukkitRunnable verifyTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    YamlFile settings = plugin.getConfiguration().get("settings");

                    String serverPath = "Servers." + serverKey;

                    String serverAddress = settings.getString(serverPath + ".Address");
                    String serverUuid = settings.getString(serverPath + ".Uuid");

                    URL url = new URL(serverAddress + "/votes/server/" + serverUuid);

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                        JsonArray entries = JsonParser.parseReader(reader).getAsJsonArray();

                        LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

                        for (JsonElement entry : entries) {
                            JsonObject entryObject = entry.getAsJsonObject();

                            JsonElement nicknameElement = entryObject.get("nickname");

                            if (nicknameElement.isJsonNull()) {
                                continue;
                            }

                            String nickname = nicknameElement.getAsString();
                            LocalDate createdAt = LocalDate.parse(
                                    entryObject.get("createdAt").getAsString(),
                                    DateTimeFormatter.ISO_DATE_TIME);

                            if (nickname.equals(playerName) && createdAt.isEqual(timeNow)) {
                                listener.success();

                                return;
                            }
                        }

                        listener.notFound();
                    }
                } catch (IOException exception) {
                    listener.exceptionCaught(exception);
                }
            }
        };

        verifyTask.runTaskAsynchronously(plugin);
    }
}
