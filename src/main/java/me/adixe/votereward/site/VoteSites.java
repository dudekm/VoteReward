package me.adixe.votereward.site;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The configured vote lists, in configuration order, looked up by name.
 */
public final class VoteSites {
    private final Map<String, VoteSite> byName;

    public VoteSites(Collection<VoteSite> sites) {
        Map<String, VoteSite> map = new LinkedHashMap<>();

        for (VoteSite site : sites) {
            if (map.putIfAbsent(site.name(), site) != null) {
                throw new IllegalArgumentException("Duplicate site name: " + site.name());
            }
        }

        this.byName = map;
    }

    public Optional<VoteSite> find(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    public List<VoteSite> all() {
        return List.copyOf(byName.values());
    }

    public boolean isEmpty() {
        return byName.isEmpty();
    }
}
