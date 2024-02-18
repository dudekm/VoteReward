package me.adixe.votereward.votemanager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adixe.votereward.VoteReward;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class VoteVerifier {
    private final Server server;
    private final String playerName;

    public VoteVerifier(Server server, String playerName) {
        this.server = server;
        this.playerName = playerName;
    }

    public void verify(VerificationListener listener) {
        Bukkit.getScheduler().runTaskAsynchronously(VoteReward.getInstance(), () -> {
            try {
                URL url = new URL(server.getAddress() + "/votes/server/" + server.getUuid());

                JsonParser jsonParser = new JsonParser();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    JsonArray entries = jsonParser.parse(reader).getAsJsonArray();

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
