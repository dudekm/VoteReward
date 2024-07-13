package me.adixe.votereward.votemanager.verifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adixe.votereward.votemanager.Server;
import org.bukkit.OfflinePlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LMVerifier extends VoteVerifier {
    public LMVerifier() {
        super(List.of(
                "https://lista-minecraft.pl",
                "https://lista-serwerow.emecz.pl",
                "https://minecraft-list.info"));
    }

    @Override
    protected boolean process(Server server, OfflinePlayer player) throws IOException {
        URL url = new URL(server.address() + "/votes/server/" + server.uuid());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            JsonParser jsonParser = new JsonParser();

            LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

            for (JsonElement entry : jsonParser.parse(reader).getAsJsonArray()) {
                JsonObject entryObject = entry.getAsJsonObject();

                JsonElement nicknameElement = entryObject.get("nickname");

                if (nicknameElement.isJsonNull()) {
                    continue;
                }

                String nickname = nicknameElement.getAsString();
                LocalDate createdAt = LocalDate.parse(
                        entryObject.get("createdAt").getAsString(),
                        DateTimeFormatter.ISO_DATE_TIME);

                if (nickname.equals(player.getName()) && createdAt.isEqual(timeNow)) {
                    return true;
                }
            }

            return false;
        }
    }
}
