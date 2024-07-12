package me.adixe.votereward.votemanager;

import me.adixe.votereward.VoteReward;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

public record Server(String name) {
    public String address() {
        return getSettings().getString("address");
    }

    public String uuid() {
        return getSettings().getString("uuid");
    }

    public List<String> rewardCommands() {
        return getSettings().getStringList("reward-commands");
    }

    private ConfigurationSection getSettings() {
        return VoteReward.getInstance().getConfiguration().get("settings")
                .getConfigurationSection("servers." + name);
    }
}
