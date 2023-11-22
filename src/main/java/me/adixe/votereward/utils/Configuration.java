package me.adixe.votereward.utils;

import me.adixe.votereward.VoteReward;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
    private final List<String> configurations = new ArrayList<>();
    private final Map<String, YamlFile> configurationFiles = new HashMap<>();

    public void register(String configurationName) {
        configurations.add(configurationName);
    }

    public void load() {
        try {
            VoteReward voteReward = VoteReward.getInstance();

            for (String configurationName : configurations) {
                File file = new File(voteReward.getDataFolder(), configurationName + ".yml");

                if (!file.exists()) {
                    Files.createDirectories(file.getParentFile().toPath());
                    Files.copy(voteReward.getResource(configurationName + ".yml"), file.toPath());
                }

                YamlFile yamlFile = YamlFile.loadConfiguration(file, true);

                configurationFiles.put(configurationName, yamlFile);

                yamlFile.save();
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public YamlFile get(String configurationName) {
        return configurationFiles.get(configurationName);
    }
}
