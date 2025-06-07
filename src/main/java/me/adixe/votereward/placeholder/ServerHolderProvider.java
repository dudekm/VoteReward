package me.adixe.votereward.placeholder;

import me.adixe.commonutilslib.placeholder.Provider;
import me.adixe.votereward.vote.ServerHolder;

import java.util.HashMap;
import java.util.Map;

public class ServerHolderProvider extends Provider<ServerHolder> {
    public ServerHolderProvider(String defaultPrefix) {
        super(ServerHolder.class, defaultPrefix);
    }

    @Override
    public Map<String, String> get(ServerHolder server) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", server.name());
        placeholders.put("address", server.address());

        return placeholders;
    }
}
