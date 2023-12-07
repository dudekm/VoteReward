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
    private static final List<String> CONFIGURATIONS = new ArrayList<>();
    private static final Map<String, YamlFile> CONFIGURATION_FILES = new HashMap<>();

    public static void register(String configurationName) {
        CONFIGURATIONS.add(configurationName);
    }

    public static void load() {
        try {
            VoteReward voteReward = VoteReward.getInstance();

            for (String configurationName : CONFIGURATIONS) {
                File file = new File(voteReward.getDataFolder(), configurationName + ".yml");

                if (!file.exists()) {
                    Files.createDirectories(file.getParentFile().toPath());
                    Files.copy(voteReward.getResource(configurationName + ".yml"), file.toPath());
                }

                YamlFile yamlFile = YamlFile.loadConfiguration(file, true);

                CONFIGURATION_FILES.put(configurationName, yamlFile);

                yamlFile.save();
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void dispose() {
        CONFIGURATIONS.clear();
        CONFIGURATION_FILES.clear();
    }

    public static YamlFile get(String configurationName) {
        return CONFIGURATION_FILES.get(configurationName);
    }
}
