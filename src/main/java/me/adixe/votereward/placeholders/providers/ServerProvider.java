package me.adixe.votereward.placeholders.providers;

import me.adixe.votereward.VoteReward;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.HashMap;
import java.util.Map;

public class ServerProvider extends PlaceholdersProvider<ServerProvider.ServerIdentifier> {
    public ServerProvider(VoteReward plugin) {
        super(plugin, ServerIdentifier.class, "server");
    }

    @Override
    public Map<String, String> get(ServerIdentifier server) {
        YamlFile settings = plugin.getConfiguration().get("settings");

        String serverKey = server.key;

        String serverPath = "Servers." + serverKey;

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", serverKey);
        placeholders.put("address", settings.getString(serverPath + ".Address"));
        placeholders.put("server_uuid", settings.getString(serverPath + ".Uuid"));

        return placeholders;
    }

    public static class ServerIdentifier {
        private final String key;

        public ServerIdentifier(String key) {
            this.key = key;
        }
    }
}
