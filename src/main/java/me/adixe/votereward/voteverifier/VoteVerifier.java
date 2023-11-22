package me.adixe.votereward.voteverifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adixe.votereward.VoteReward;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class VoteVerifier {
    private final String server;
    private final String playerName;

    public VoteVerifier(String server, String playerName) {
        this.server = server;
        this.playerName = playerName;
    }

    public void verify(VerificationListener listener) {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        VoteReward voteReward = VoteReward.getInstance();

        scheduler.runTaskAsynchronously(voteReward, () -> {
            YamlFile settings = voteReward.getConfiguration().get("settings");

            try {
                String serverSettingsPath = "Servers." + server;

                String serverAddress = settings.getString(serverSettingsPath + ".Address");
                String serverUuid = settings.getString(serverSettingsPath + ".Uuid");

                URL url = new URL(serverAddress + "/votes/server/" + serverUuid);

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    JsonArray entries = JsonParser.parseReader(reader).getAsJsonArray();

                    LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

                    for (JsonElement entry : entries) {
                        JsonObject entryObject = entry.getAsJsonObject();

                        JsonElement nicknameElement = entryObject.get("nickname");

                        if (nicknameElement.isJsonNull())
                            continue;

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
            } catch (Exception exception) {
                listener.exceptionCaught(exception);
            }
        });
    }
}
