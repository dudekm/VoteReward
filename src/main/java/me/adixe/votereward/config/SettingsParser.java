package me.adixe.votereward.config;

import me.adixe.votereward.site.ListAddress;
import me.adixe.votereward.site.RewardCommand;
import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.site.VoteSites;
import me.adixe.votereward.vote.ServerId;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds {@link Settings} from {@code settings.yml}. A wrong value never stops the plugin:
 * it falls back to the default (or the list entry is skipped) and a warning says why,
 * so one typo does not switch off rewards for every list.
 */
public final class SettingsParser {
    public Result parse(ConfigView config) {
        List<String> warnings = new ArrayList<>();

        return new Result(new Settings(pollInterval(config, warnings), sites(config, warnings)),
                List.copyOf(warnings));
    }

    private static Duration pollInterval(ConfigView config, List<String> warnings) {
        Optional<String> raw = config.string("polling.interval").filter(value -> !value.isBlank());

        if (raw.isEmpty()) {
            return Settings.DEFAULT_POLL_INTERVAL;
        }

        Duration interval;

        try {
            interval = Durations.parse(raw.get());
        } catch (IllegalArgumentException exception) {
            warnings.add("polling.interval: " + exception.getMessage() + " Using "
                    + Durations.format(Settings.DEFAULT_POLL_INTERVAL) + ".");

            return Settings.DEFAULT_POLL_INTERVAL;
        }

        if (interval.compareTo(Settings.MIN_POLL_INTERVAL) < 0) {
            warnings.add("polling.interval is below the minimum of " + Durations.format(Settings.MIN_POLL_INTERVAL)
                    + " — using " + Durations.format(Settings.MIN_POLL_INTERVAL) + ".");

            return Settings.MIN_POLL_INTERVAL;
        }

        return interval;
    }

    private static VoteSites sites(ConfigView config, List<String> warnings) {
        List<VoteSite> sites = new ArrayList<>();

        for (String name : config.childKeys("servers")) {
            String path = "servers." + name;

            try {
                ListAddress address = ListAddress.of(required(config, path + ".address"));
                ServerId serverId = ServerId.of(required(config, path + ".uuid"));
                List<RewardCommand> commands = new ArrayList<>();

                for (String command : config.stringList(path + ".reward-commands")) {
                    if (!command.isBlank()) {
                        commands.add(new RewardCommand(command));
                    }
                }

                if (commands.isEmpty()) {
                    warnings.add(path + " has no reward-commands — votes there will give nothing.");
                }

                sites.add(new VoteSite(name, address, serverId, commands));
            } catch (IllegalArgumentException exception) {
                warnings.add(path + " skipped: " + exception.getMessage());
            }
        }

        if (sites.isEmpty()) {
            warnings.add("No valid entries under \"servers\" — no votes will be checked.");
        }

        return new VoteSites(sites);
    }

    private static String required(ConfigView config, String path) {
        return config.string(path)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("missing \"" + path + "\"."));
    }

    /**
     * @param warnings problems found in the file, for the server log
     */
    public record Result(Settings settings, List<String> warnings) {
    }
}
