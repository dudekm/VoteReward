package me.adixe.votereward.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ConfigView} over nested maps, standing in for a YAML file in tests.
 */
public final class MapConfigView implements ConfigView {
    private final Map<String, Object> root;

    public MapConfigView(Map<String, Object> root) {
        this.root = root;
    }

    @Override
    public Optional<String> string(String path) {
        Object value = get(path);

        return value == null || value instanceof Map || value instanceof List
                ? Optional.empty()
                : Optional.of(String.valueOf(value));
    }

    @Override
    public List<String> stringList(String path) {
        List<String> result = new ArrayList<>();

        if (get(path) instanceof List<?> list) {
            list.forEach(item -> result.add(String.valueOf(item)));
        }

        return result;
    }

    @Override
    public Set<String> childKeys(String path) {
        return get(path) instanceof Map<?, ?> map ? new LinkedHashSet<>(castKeys(map)) : Set.of();
    }

    private Object get(String path) {
        Object current = root;

        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }

            current = map.get(part);
        }

        return current;
    }

    private static Set<String> castKeys(Map<?, ?> map) {
        Set<String> keys = new LinkedHashSet<>();
        map.keySet().forEach(key -> keys.add(String.valueOf(key)));

        return keys;
    }
}
