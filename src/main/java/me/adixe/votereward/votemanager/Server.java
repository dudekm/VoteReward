package me.adixe.votereward.votemanager;

import me.adixe.votereward.VoteReward;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.List;

public class Server {
    private final String name;

    public Server(String name) {
        this.name = name;
    }

    public String getAddress() {
        return getSettings().getString("address");
    }

    public String getUuid() {
        return getSettings().getString("uuid");
    }

    public List<String> getRewardCommands() {
        return getSettings().getStringList("reward-commands");
    }

    private ConfigurationSection getSettings() {
        YamlFile settings = VoteReward.getInstance().getConfiguration().get("settings");

        return settings.getConfigurationSection("servers." + name);
    }

    public String getName() {
        return name;
    }
}
