package me.adixe.votereward.vote.verifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adixe.votereward.vote.ServerHolder;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.entity.Player;

import java.io.IOException;
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
    protected boolean process(Player player, ServerHolder server) throws IOException {
        HttpUrl.Builder builder = HttpUrl.parse(server.address()).newBuilder()
                .addPathSegments("votes/server")
                .addPathSegment(server.settings().getString("uuid"));

        Request request = new Request.Builder()
                .url(builder.build())
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            LocalDate timeNow = LocalDate.now(ZoneId.of("Europe/Paris"));

            JsonArray entries = JsonParser.parseReader(response.body().charStream()).getAsJsonArray();

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

                if (nickname.equalsIgnoreCase(player.getName()) && createdAt.isEqual(timeNow)) {
                    return true;
                }
            }

            return false;
        }
    }
}
