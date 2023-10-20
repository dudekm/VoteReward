package me.adixe.votereward;

import me.adixe.votereward.commands.ReloadCommand;
import me.adixe.votereward.commands.RewardCommand;
import me.adixe.votereward.utils.Configuration;
import me.adixe.votereward.utils.MessagesUtility;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class VoteReward extends JavaPlugin {
    private Configuration configuration;
    private MessagesUtility messagesUtility;

    @Override
    public void onEnable() {
        configuration = new Configuration(this);
        configuration.register("settings");
        configuration.register("data");
        configuration.load();

        messagesUtility = new MessagesUtility(this);

        getCommand("reward").setExecutor(new RewardCommand(this));
        getCommand("reload").setExecutor(new ReloadCommand(this));

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            YamlFile data = configuration.get("data");

            LocalDate now = LocalDate.now();

            if (!data.contains("Date") || !LocalDate.parse(data.getString("Date"),
                    DateTimeFormatter.ISO_LOCAL_DATE).isEqual(now)) {
                data.set("Date", now.format(DateTimeFormatter.ISO_LOCAL_DATE));

                data.set("Votes", null);
            }

            saveData();
        }, 0L, 20L * 60 * 10);

        PluginDescriptionFile description = getDescription();

        getLogger().info(description.getName() + " v" + description.getVersion() + " by " +
                String.join(", ", description.getAuthors()) + " enabled.");
    }

    @Override
    public void onDisable() {
        saveData();

        PluginDescriptionFile description = getDescription();

        getLogger().info(description.getName() + " v" + description.getVersion() + " by " +
                String.join(", ", description.getAuthors()) + " disabled.");
    }

    private void saveData() {
        try {
            configuration.get("data").save();
        } catch (IOException exception) {
            getLogger().log(Level.SEVERE,
                    "An error occurred while trying to save data.", exception);
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public MessagesUtility getMessagesUtility() {
        return messagesUtility;
    }
}
