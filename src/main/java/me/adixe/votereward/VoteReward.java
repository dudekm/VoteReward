package me.adixe.votereward;

import me.adixe.commonutilslib.command.CommandService;
import me.adixe.commonutilslib.configuration.Configuration;
import me.adixe.commonutilslib.configuration.SectionHolder;
import me.adixe.commonutilslib.placeholder.PlaceholderManager;
import me.adixe.commonutilslib.placeholder.PlayerProvider;
import me.adixe.commonutilslib.util.MessageUtil;
import me.adixe.votereward.commands.CheckCommand;
import me.adixe.votereward.commands.ReloadCommand;
import me.adixe.votereward.commands.RewardCommand;
import me.adixe.votereward.placeholder.ServerHolderProvider;
import me.adixe.votereward.vote.VoteManager;
import me.adixe.votereward.vote.verifier.LMVerifier;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VoteReward extends JavaPlugin {
    private static VoteReward instance;

    private Configuration configuration;
    private PlaceholderManager placeholderManager;
    private VoteManager voteManager;

    @Override
    public void onEnable() {
        instance = this;

        loadConfiguration();

        loadPlaceholders();

        loadCommands();

        loadVoteManager();

        loadMetrics();

        MessageUtil.registerAudiences(this);

        getLogger().info("Enabled successfully.");
    }

    private void loadConfiguration() {
        configuration = new Configuration(this);
        configuration.registerFile("settings.yml", "settings.yml");
        configuration.registerFile("messages.yml", "messages.yml");
        configuration.registerDataFile("data/votes.yml", 10);

        try {
            configuration.reload();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadPlaceholders() {
        placeholderManager = new PlaceholderManager();
        placeholderManager.register(new PlayerProvider("player"));
        placeholderManager.register(new ServerHolderProvider("server"));
    }

    private void loadCommands() {
        SectionHolder messagesHolder = new SectionHolder(
                configuration, "messages", "commands");

        CommandService commandService = new CommandService("votereward",
                "votereward.command", messagesHolder, false);

        commandService.register(new RewardCommand("reward",
                "votereward.reward",
                messagesHolder), "reward");
        commandService.register(new CheckCommand("check",
                "votereward.check",
                messagesHolder), "check");
        commandService.register(new ReloadCommand("reload",
                "votereward.reload",
                messagesHolder), "reload");

        commandService.register(this);
    }

    private void loadVoteManager() {
        voteManager = new VoteManager();
        voteManager.register(new LMVerifier());
    }

    private void loadMetrics() {
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
    }

    @Override
    public void onDisable() {
        configuration.save("votes");

        MessageUtil.close();

        getLogger().info("Disabled successfully.");
    }

    public static VoteReward getInstance() {
        return instance;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }
}
