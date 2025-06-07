package me.adixe.votereward.vote.verifier;

import me.adixe.votereward.vote.ServerHolder;
import okhttp3.OkHttpClient;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public abstract class VoteVerifier {
    private final List<String> addresses;
    private final OkHttpClient client;

    public VoteVerifier(List<String> addresses) {
        this.addresses = addresses;
        this.client = new OkHttpClient();
    }

    public boolean verify(Player player, ServerHolder server) throws IOException {
        if (!canVerify(server)) {
            throw new IllegalArgumentException("Invalid server address: " + server.address());
        }

        return process(player, server);
    }

    protected abstract boolean process(Player player, ServerHolder server) throws IOException;

    public boolean canVerify(ServerHolder server) {
        return addresses.contains(server.address());
    }

    public OkHttpClient getClient() {
        return client;
    }
}
