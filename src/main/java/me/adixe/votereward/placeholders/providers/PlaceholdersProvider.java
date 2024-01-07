package me.adixe.votereward.placeholders.providers;

import me.adixe.votereward.VoteReward;

import java.util.HashMap;
import java.util.Map;

public abstract class PlaceholdersProvider<T> {
    protected final VoteReward plugin;
    private final Class<T> type;
    private final String defaultPrefix;

    public PlaceholdersProvider(VoteReward plugin, Class<T> type, String defaultPrefix) {
        this.plugin = plugin;
        this.type = type;
        this.defaultPrefix = defaultPrefix;
    }

    public abstract Map<String, String> get(T object);

    public Map<String, String> getUnique(T object, String prefix) {
        Map<String, String> placeholders = get(object);

        Map<String, String> newPlaceholders = new HashMap<>();

        placeholders.forEach((key, value) -> newPlaceholders.put(prefix + "_" + key, value));

        return newPlaceholders;
    }

    public Map<String, String> getUnique(T object) {
        return getUnique(object, defaultPrefix);
    }

    public Class<T> getType() {
        return type;
    }
}
