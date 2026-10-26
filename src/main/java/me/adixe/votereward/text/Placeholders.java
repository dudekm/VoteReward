package me.adixe.votereward.text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable set of {@code %name%} placeholders substituted into messages and commands.
 */
public final class Placeholders {
    private static final Placeholders EMPTY = new Placeholders(Collections.emptyMap());

    private final Map<String, String> values;

    private Placeholders(Map<String, String> values) {
        this.values = values;
    }

    public static Placeholders empty() {
        return EMPTY;
    }

    public static Placeholders of(String key, Object value) {
        return EMPTY.with(key, value);
    }

    public Placeholders with(String key, Object value) {
        Map<String, String> copy = new LinkedHashMap<>(values);
        copy.put(key, String.valueOf(value));

        return new Placeholders(Collections.unmodifiableMap(copy));
    }

    public Placeholders with(Placeholders other) {
        Map<String, String> copy = new LinkedHashMap<>(values);
        copy.putAll(other.values);

        return new Placeholders(Collections.unmodifiableMap(copy));
    }

    public String apply(String text) {
        String result = text;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return result;
    }

    public Map<String, String> asMap() {
        return values;
    }
}
