package me.adixe.votereward.bukkit;

import me.adixe.votereward.config.ConfigView;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ConfigView} over a Bukkit YAML file.
 */
public final class BukkitConfigView implements ConfigView {
    private final ConfigurationSection section;

    public BukkitConfigView(ConfigurationSection section) {
        this.section = section;
    }

    @Override
    public Optional<String> string(String path) {
        // getString also turns numbers into text, so "interval: 30" works as well as "30s".
        return Optional.ofNullable(section.getString(path));
    }

    @Override
    public List<String> stringList(String path) {
        return section.getStringList(path);
    }

    @Override
    public Set<String> childKeys(String path) {
        ConfigurationSection child = section.getConfigurationSection(path);

        return child == null ? Set.of() : new LinkedHashSet<>(child.getKeys(false));
    }
}
