package me.adixe.votereward.configuration;

import me.adixe.votereward.VoteReward;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private final VoteReward plugin;
    private final Map<String, YamlFile> files;

    public Configuration(VoteReward plugin) {
        this.plugin = plugin;
        this.files = new HashMap<>();
    }

    public void register(String path) {
        files.put(path, load(path));
    }

    public void reload() {
        files.entrySet().forEach(entry -> entry.setValue(load(entry.getKey())));
    }

    private YamlFile load(String path) {
        try {
            String fileName = path + ".yml";

            File file = new File(plugin.getDataFolder(), fileName);

            if (!file.exists()) {
                Files.createDirectories(file.getParentFile().toPath());
                Files.copy(plugin.getResource(fileName), file.toPath());
            }

            YamlFile yamlFile = YamlFile.loadConfiguration(file, true);

            yamlFile.save();

            return yamlFile;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public YamlFile get(String path) {
        return files.get(path);
    }
}
