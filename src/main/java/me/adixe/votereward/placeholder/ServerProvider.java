package me.adixe.votereward.placeholder;

import me.adixe.commonutilslib.placeholder.provider.PlaceholderProvider;
import me.adixe.votereward.votemanager.Server;

import java.util.HashMap;
import java.util.Map;

public class ServerProvider extends PlaceholderProvider<Server> {
    public ServerProvider(String defaultPrefix) {
        super(Server.class, defaultPrefix);
    }

    @Override
    public Map<String, String> get(Server server) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", server.name());
        placeholders.put("address", server.address());
        placeholders.put("uuid", server.uuid());

        return placeholders;
    }
}
