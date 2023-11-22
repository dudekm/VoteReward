package me.adixe.votereward;

import me.adixe.votereward.commands.CommandsService;
import me.adixe.votereward.commands.executors.CheckCommand;
import me.adixe.votereward.commands.executors.ReloadCommand;
import me.adixe.votereward.commands.executors.RewardCommand;
import me.adixe.votereward.utils.Configuration;
import me.adixe.votereward.utils.MessagesUtility;
import me.adixe.votereward.utils.PlaceholdersProvider;
import me.adixe.votereward.utils.VoteManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.HashMap;
import java.util.Map;

public class VoteReward extends JavaPlugin {
    private static VoteReward instance;

    private Configuration configuration;
    private PlaceholdersProvider placeholdersProvider;
    private VoteManager voteManager;
    private MessagesUtility messagesUtility;

    @Override
    public void onEnable() {
        instance = this;

        configuration = new Configuration();
        configuration.register("settings");
        configuration.register("data");
        configuration.load();

        placeholdersProvider = new PlaceholdersProvider();

        voteManager = new VoteManager();
        voteManager.setup();

        messagesUtility = new MessagesUtility();

        CommandsService commandsService = new CommandsService();
        commandsService.register(new RewardCommand(), "reward");
        commandsService.register(new CheckCommand(), "check");
        commandsService.register(new ReloadCommand(), "reload");

        getCommand("votereward").setExecutor(commandsService);

        Metrics metrics = new Metrics(this, 20120);

        metrics.addCustomChart(new AdvancedPie("siteAddress", () -> {
            YamlFile settings = configuration.get("settings");

            Map<String, Integer> map = new HashMap<>();

            for (String key : settings.getConfigurationSection("Servers").getKeys(false)) {
                String address = settings.getString("Servers." + key + ".Address");

                map.put(address, map.getOrDefault(address, 0) + 1);
            }

            return map;
        }));

        PluginDescriptionFile description = getDescription();

        getLogger().info(description.getName() + " v" + description.getVersion() + " by " +
                String.join(", ", description.getAuthors()) + " enabled.");
    }

    @Override
    public void onDisable() {
        voteManager.saveData();

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

    public PlaceholdersProvider getPlaceholdersProvider() {
        return placeholdersProvider;
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }

    public MessagesUtility getMessagesUtility() {
        return messagesUtility;
    }
}