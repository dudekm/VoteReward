package me.adixe.votereward.votemanager.verifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adixe.votereward.votemanager.Server;
import org.bukkit.OfflinePlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SMVerifier extends VoteVerifier {
    private static final String ALREADY_VOTED = "&cNagroda zostala juz odebrana, odbierz nastepna jutro";
    private static final String NOT_FOUND = "&cNie znaleziono glosu";

    public SMVerifier() {
        super(List.of("https://serwery-minecraft.pl"));
    }

    @Override
    protected boolean process(Server server, OfflinePlayer player) throws IOException {
        URL url = new URL(server.address() + "/api/server-by-key/" +
                server.uuid() + "/get-vote/" + player.getName());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            JsonParser jsonParser = new JsonParser();

            JsonObject entryObject = jsonParser.parse(reader).getAsJsonObject();

            boolean canClaimReward = entryObject.get("can_claim_reward").getAsBoolean();
            String error = entryObject.get("error").getAsString();

            if (canClaimReward || error.equals(ALREADY_VOTED)) {
                return true;
            } else if (error.equals(NOT_FOUND)) {
                return false;
            } else {
                throw new IllegalStateException(error);
            }
        }
    }
}
