package me.adixe.votereward.vote;

import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

public record ServerHolder(ConfigurationSection settings) {
    public String name() {
        return settings.getName();
    }

    public String address() {
        return settings.getString("address");
    }

    public List<String> rewardCommands() {
        return settings.getStringList("reward-commands");
    }
}
