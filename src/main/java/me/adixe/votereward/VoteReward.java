package me.adixe.votereward;

import me.adixe.votereward.commands.CommandsService;
import me.adixe.votereward.commands.executors.CheckCommand;
import me.adixe.votereward.commands.executors.ReloadCommand;
import me.adixe.votereward.commands.executors.RewardCommand;
import me.adixe.votereward.configuration.Configuration;
import me.adixe.votereward.placeholders.PlaceholdersManager;
import me.adixe.votereward.placeholders.providers.CommandSenderProvider;
import me.adixe.votereward.placeholders.providers.PlayerProvider;
import me.adixe.votereward.placeholders.providers.ServerProvider;
import me.adixe.votereward.votemanager.VoteManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.HashMap;
import java.util.Map;

public class VoteReward extends JavaPlugin {
    private Configuration configuration;
    private PlaceholdersManager placeholdersManager;
    private VoteManager voteManager;

    @Override
    public void onEnable() {
        configuration = new Configuration(this);
        configuration.register("settings");
        configuration.register("messages");
        configuration.register("data");

        placeholdersManager = new PlaceholdersManager();
        placeholdersManager.register(new CommandSenderProvider(this));
        placeholdersManager.register(new PlayerProvider(this));
        placeholdersManager.register(new ServerProvider(this));

        voteManager = new VoteManager(this);
        voteManager.autoSave();

        CommandsService commandsService = new CommandsService(this);
        commandsService.register(new RewardCommand(this), "reward");
        commandsService.register(new CheckCommand(this), "check");
        commandsService.register(new ReloadCommand(this), "reload");

        getCommand("votereward").setExecutor(commandsService);

        Metrics metrics = new Metrics(this, 20120);

        metrics.addCustomChart(new AdvancedPie("siteAddress", () -> {
            YamlFile settings = configuration.get("settings");

            Map<String, Integer> addresses = new HashMap<>();

            for (String serverKey : settings.getConfigurationSection("Servers").getKeys(false)) {
                String address = settings.getString("Servers." + serverKey + ".Address");

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
        voteManager.save();

        PluginDescriptionFile description = getDescription();

        getLogger().info(description.getName() + " v" + description.getVersion() + " by " +
                String.join(", ", description.getAuthors()) + " disabled.");
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