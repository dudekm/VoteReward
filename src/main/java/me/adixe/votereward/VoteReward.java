package me.adixe.votereward;

import me.adixe.votereward.api.JdkHttpTransport;
import me.adixe.votereward.api.ListApiVoteSource;
import me.adixe.votereward.api.VotePageParser;
import me.adixe.votereward.api.VoteSource;
import me.adixe.votereward.bukkit.BukkitConfigView;
import me.adixe.votereward.bukkit.BukkitRewardDispatcher;
import me.adixe.votereward.bukkit.Messages;
import me.adixe.votereward.bukkit.PlayerJoinListener;
import me.adixe.votereward.bukkit.TaskScheduler;
import me.adixe.votereward.bukkit.VoteRewardCommand;
import me.adixe.votereward.config.Durations;
import me.adixe.votereward.config.Settings;
import me.adixe.votereward.config.SettingsParser;
import me.adixe.votereward.ledger.LedgerRepository;
import me.adixe.votereward.ledger.VoteLedger;
import me.adixe.votereward.ledger.YamlLedgerRepository;
import me.adixe.votereward.reward.PollReport;
import me.adixe.votereward.reward.RewardService;
import me.adixe.votereward.reward.VotePoller;
import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.vote.Nickname;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Plugin entry point: wires the pieces together and owns their lifecycle.
 */
public final class VoteReward extends JavaPlugin {
    private static final int BSTATS_ID = 20120;

    private VoteSource source;
    private TaskScheduler scheduler;
    private LedgerRepository repository;
    private VoteLedger ledger;

    private volatile Settings settings;
    private volatile Messages messages;
    private volatile RewardService rewards;
    private volatile VotePoller poller;

    @Override
    public void onEnable() {
        scheduler = TaskScheduler.detect(this);
        source = new ListApiVoteSource(new JdkHttpTransport(), new VotePageParser(),
                "VoteReward/" + getDescription().getVersion());
        repository = new YamlLedgerRepository(getDataFolder().toPath().resolve("data").resolve("ledger.yml"));

        try {
            ledger = VoteLedger.restore(repository.load());
        } catch (IOException exception) {
            getLogger().log(Level.SEVERE, "Could not read the vote data — starting with an empty one.", exception);
            ledger = VoteLedger.empty();
        }

        if (!reload()) {
            getLogger().severe("Could not load the configuration — the plugin is disabled.");
            getServer().getPluginManager().disablePlugin(this);

            return;
        }

        PluginCommand command = getCommand("votereward");
        VoteRewardCommand executor = new VoteRewardCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        startMetrics();

        getLogger().info("Running on " + Bukkit.getName() + " " + Bukkit.getBukkitVersion() + ".");
    }

    @Override
    public void onDisable() {
        if (scheduler != null) {
            scheduler.cancelAll();
        }

        if (poller != null) {
            poller.save();
        }
    }

    /**
     * (Re)reads both configuration files and restarts the vote checks with the new settings.
     *
     * @return false when a file could not be read; the previous configuration then stays active
     */
    public synchronized boolean reload() {
        Settings newSettings;
        Messages newMessages;

        try {
            newSettings = loadSettings();
            newMessages = new Messages(loadYaml("messages.yml", true));
        } catch (IOException exception) {
            getLogger().log(Level.SEVERE, "Could not read the configuration.", exception);

            return false;
        }

        scheduler.cancelAll();

        Set<String> siteNames = new HashSet<>();
        newSettings.sites().all().forEach(site -> siteNames.add(site.name()));
        ledger.retainSites(siteNames);

        settings = newSettings;
        messages = newMessages;
        rewards = new RewardService(ledger, newSettings.sites(),
                nickname -> Bukkit.getPlayerExact(nickname.value()) != null,
                new BukkitRewardDispatcher(scheduler, newMessages), getLogger());
        poller = new VotePoller(newSettings, source, ledger, repository, rewards, Clock.systemUTC(), getLogger());

        VotePoller activePoller = poller;
        scheduler.runAsync(activePoller::poll);
        scheduler.runAsyncRepeating(activePoller::poll, newSettings.pollInterval());

        getLogger().info("Checking " + newSettings.sites().all().size() + " vote list(s) every "
                + Durations.format(newSettings.pollInterval()) + ".");

        return true;
    }

    /**
     * Gives the player whatever is waiting for them.
     *
     * @return how many rewards were given
     */
    public int deliver(Nickname player) {
        int delivered = rewards.deliver(player);

        if (delivered > 0) {
            VotePoller activePoller = poller;
            scheduler.runAsync(activePoller::save);
        }

        return delivered;
    }

    /** Checks every list right away instead of waiting for the next scheduled check. */
    public void pollNow(Consumer<PollReport> whenDone) {
        VotePoller activePoller = poller;
        scheduler.runAsync(() -> {
            PollReport report = activePoller.poll();
            scheduler.runSync(() -> whenDone.accept(report));
        });
    }

    public Settings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public VoteLedger ledger() {
        return ledger;
    }

    private Settings loadSettings() throws IOException {
        SettingsParser.Result result = new SettingsParser().parse(new BukkitConfigView(loadYaml("settings.yml", false)));

        for (String warning : result.warnings()) {
            getLogger().warning("settings.yml: " + warning);
        }

        return result.settings();
    }

    /**
     * Reads a file from the plugin folder, creating it from the jar's copy first.
     *
     * @param withDefaults whether the jar's copy fills in missing keys — right for messages, wrong for
     *                     settings, where it would bring back the example lists the owner removed
     */
    private YamlConfiguration loadYaml(String name, boolean withDefaults) throws IOException {
        Path file = getDataFolder().toPath().resolve(name);

        if (!Files.exists(file)) {
            saveResource(name, false);
        }

        YamlConfiguration configuration = new YamlConfiguration();

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            configuration.load(reader);
        } catch (org.bukkit.configuration.InvalidConfigurationException exception) {
            throw new IOException(name + " is not valid YAML: " + exception.getMessage(), exception);
        }

        if (!withDefaults) {
            return configuration;
        }

        try (InputStream defaults = getResource(name)) {
            if (defaults != null) {
                configuration.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaults, StandardCharsets.UTF_8)));
            }
        }

        return configuration;
    }

    private void startMetrics() {
        Metrics metrics = new Metrics(this, BSTATS_ID);

        metrics.addCustomChart(new AdvancedPie("siteAddress", () -> {
            Map<String, Integer> addresses = new HashMap<>();

            for (VoteSite site : settings.sites().all()) {
                addresses.merge(site.address().toString(), 1, Integer::sum);
            }

            return addresses;
        }));
    }
}
