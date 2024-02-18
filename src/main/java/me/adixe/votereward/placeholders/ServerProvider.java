package me.adixe.votereward.placeholders;

import me.adixe.commonutilslib.placeholders.providers.PlaceholdersProvider;
import me.adixe.votereward.votemanager.Server;

import java.util.HashMap;
import java.util.Map;

public class ServerProvider extends PlaceholdersProvider<Server> {
    public ServerProvider() {
        super(Server.class, "server");
    }

    @Override
    public Map<String, String> get(Server server) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", server.getName());
        placeholders.put("address", server.getAddress());
        placeholders.put("uuid", server.getUuid());

        return placeholders;
    }
}
