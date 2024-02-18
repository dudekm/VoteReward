package me.adixe.votereward;

import me.adixe.commonutilslib.commands.CommandsService;
import me.adixe.commonutilslib.configuration.Configuration;
import me.adixe.commonutilslib.configuration.SectionContainer;
import me.adixe.commonutilslib.placeholders.PlaceholdersManager;
import me.adixe.commonutilslib.placeholders.providers.PlayerProvider;
import me.adixe.votereward.commands.CheckCommand;
import me.adixe.votereward.commands.ReloadCommand;
import me.adixe.votereward.commands.RewardCommand;
import me.adixe.votereward.placeholders.ServerProvider;
import me.adixe.votereward.votemanager.VoteManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class VoteReward extends JavaPlugin {
    private static VoteReward instance;

    private Configuration configuration;
    private PlaceholdersManager placeholdersManager;
    private VoteManager voteManager;

    @Override
    public void onEnable() {
        instance = this;

        configuration = new Configuration(this);
        configuration.registerFile("settings.yml", "settings.yml");
        configuration.registerFile("messages.yml", "messages.yml");
        configuration.registerDataFile("votes.yml", 10);

        try {
            configuration.reload();
        } catch (IOException exception) {
            getLogger().log(Level.SEVERE,
                    "An error occurred while loading configuration.",
                    exception);
        }

        placeholdersManager = new PlaceholdersManager();
        placeholdersManager.register(new PlayerProvider());
        placeholdersManager.register(new ServerProvider());

        SectionContainer commandsSettings = new SectionContainer(
                configuration, "messages", "commands");

        CommandsService commandsService = new CommandsService(
                this, commandsSettings, "votereward");
        commandsService.register(new RewardCommand(
                "votereward.reward",
                commandsSettings), "reward");
        commandsService.register(new CheckCommand(
                "votereward.check",
                commandsSettings), "check");
        commandsService.register(new ReloadCommand(
                "votereward.reload",
                commandsSettings), "reload");

        voteManager = new VoteManager();

        Metrics metrics = new Metrics(this, 20120);

        metrics.addCustomChart(new AdvancedPie("siteAddress", () -> {
            YamlFile settings = configuration.get("settings");

            Map<String, Integer> addresses = new HashMap<>();

            for (String key : settings.getConfigurationSection("servers").getKeys(false)) {
                String address = settings.getString("servers." + key + ".address");

                addresses.put(address, addresses.getOrDefault(address, 0) + 1);
            }

            return addresses;
        }));

        PluginDescriptionFile description = getDescription();

        getLogger().info(description.getName() + " v" + description.getVersion() + " by " +
                String.join(", ", description.getAuthors()) + " enabled.");
    }

    @Override
    public void onDisable() {
        configuration.saveAll();

        PluginDescriptionFile description = getDescription();

        getLogger().info(description.getName() + " v" + description.getVersion() + " by " +
                String.join(", ", description.getAuthors()) + " disabled.");
    }

    public static VoteReward getInstance() {
        return instance;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public PlaceholdersManager getPlaceholdersManager() {
        return placeholdersManager;
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }
}