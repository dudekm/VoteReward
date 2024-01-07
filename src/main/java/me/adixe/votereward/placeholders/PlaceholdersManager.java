package me.adixe.votereward.placeholders;

import me.adixe.votereward.placeholders.providers.PlaceholdersProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaceholdersManager {
    private final List<PlaceholdersProvider<?>> placeholdersProviders = new ArrayList<>();

    public <T> void register(PlaceholdersProvider<T> placeholdersProvider) {
        placeholdersProviders.add(placeholdersProvider);
    }

    @SuppressWarnings("unchecked")
    public <T> PlaceholdersProvider<T> get(Class<T> type) {
        return (PlaceholdersProvider<T>) placeholdersProviders.stream()
                .filter(placeholdersProvider -> placeholdersProvider.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    public static String translate(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return text;
    }
}
