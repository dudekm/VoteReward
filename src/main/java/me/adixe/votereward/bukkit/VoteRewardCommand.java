package me.adixe.votereward.bukkit;

import me.adixe.votereward.VoteReward;
import me.adixe.votereward.config.Durations;
import me.adixe.votereward.ledger.PendingReward;
import me.adixe.votereward.site.VoteSite;
import me.adixe.votereward.text.Placeholders;
import me.adixe.votereward.vote.Nickname;
import me.adixe.votereward.vote.VoteDay;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@code /votereward [reward | check [player] | reload | poll]}, alias {@code /vr}.
 */
public final class VoteRewardCommand implements TabExecutor {
    private static final List<String> SUBCOMMANDS = List.of("reward", "check", "reload", "poll");

    private final VoteReward plugin;

    public VoteRewardCommand(VoteReward plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subcommand = args.length == 0 ? "reward" : args[0].toLowerCase(Locale.ROOT);

        if (!SUBCOMMANDS.contains(subcommand)) {
            plugin.messages().send(sender, "command.usage");

            return true;
        }

        if (!sender.hasPermission("votereward." + subcommand)) {
            plugin.messages().send(sender, "command.no-permission");

            return true;
        }

        switch (subcommand) {
            case "reward" -> reward(sender);
            case "check" -> check(sender, args);
            case "reload" -> reload(sender);
            default -> poll(sender);
        }

        return true;
    }

    private void reward(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "command.only-player");

            return;
        }

        if (plugin.deliver(Nickname.of(player.getName())) == 0) {
            plugin.messages().send(sender, "command.reward.none", intervalPlaceholder());
        }
    }

    private void check(CommandSender sender, String[] args) {
        Nickname nickname;

        if (args.length > 1) {
            if (!sender.hasPermission("votereward.check.others")) {
                plugin.messages().send(sender, "command.no-permission");

                return;
            }

            nickname = Nickname.of(args[1]);
        } else if (sender instanceof Player player) {
            nickname = Nickname.of(player.getName());
        } else {
            plugin.messages().send(sender, "command.check.no-player");

            return;
        }

        LocalDate today = VoteDay.of(Instant.now());
        List<PendingReward> pending = plugin.ledger().pendingFor(nickname);
        Placeholders player = Placeholders.of("player_name", nickname.value());

        plugin.messages().send(sender, "command.check.header", player);

        for (VoteSite site : plugin.settings().sites().all()) {
            Placeholders placeholders = player
                    .with("server_name", site.name())
                    .with("server_address", site.address());

            if (pending.stream().anyMatch(reward -> reward.site().equals(site.name()))) {
                plugin.messages().send(sender, "command.check.pending", placeholders);
            } else if (plugin.ledger().wasRewarded(site.name(), nickname, today)) {
                plugin.messages().send(sender, "command.check.rewarded", placeholders);
            } else {
                plugin.messages().send(sender, "command.check.not-voted", placeholders);
            }
        }
    }

    private void reload(CommandSender sender) {
        if (plugin.reload()) {
            plugin.messages().send(sender, "command.reload.done", intervalPlaceholder());
        } else {
            plugin.messages().send(sender, "command.reload.failed");
        }
    }

    private void poll(CommandSender sender) {
        plugin.messages().send(sender, "command.poll.started");
        plugin.pollNow(report -> plugin.messages().send(sender, "command.poll.done",
                Placeholders.of("checked", report.sitesChecked())
                        .with("failed", report.sitesFailed())
                        .with("votes", report.votesRead())
                        .with("rewards", report.rewardsEarned())));
    }

    private Placeholders intervalPlaceholder() {
        return Placeholders.of("interval", Durations.format(plugin.settings().pollInterval()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> matches = new ArrayList<>();

        if (args.length == 1) {
            for (String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(args[0].toLowerCase(Locale.ROOT))
                        && sender.hasPermission("votereward." + subcommand)) {
                    matches.add(subcommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("check")
                && sender.hasPermission("votereward.check.others")) {
            for (Player player : sender.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    matches.add(player.getName());
                }
            }
        }

        return matches;
    }
}
