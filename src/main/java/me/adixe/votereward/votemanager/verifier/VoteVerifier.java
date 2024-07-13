package me.adixe.votereward.votemanager.verifier;

import me.adixe.votereward.votemanager.Server;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.List;

public abstract class VoteVerifier {
    private final List<String> addresses;

    public VoteVerifier(List<String> addresses) {
        this.addresses = addresses;
    }

    public boolean verify(Server server, OfflinePlayer player) throws IOException {
        if (!canVerify(server)) {
            throw new IllegalArgumentException("Invalid server address: " + server.address());
        }

        return process(server, player);
    }

    protected abstract boolean process(Server server, OfflinePlayer player) throws IOException;

    public boolean canVerify(Server server) {
        return addresses.contains(server.address());
    }
}
