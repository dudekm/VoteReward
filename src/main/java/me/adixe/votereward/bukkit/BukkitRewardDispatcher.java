package me.adixe.votereward.bukkit;

import me.adixe.votereward.ledger.PendingReward;
import me.adixe.votereward.reward.Reward;
import me.adixe.votereward.reward.RewardDispatcher;
import me.adixe.votereward.site.RewardCommand;
import me.adixe.votereward.text.Placeholders;
import me.adixe.votereward.vote.Nickname;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

/**
 * Runs the reward commands from the console and thanks the player.
 */
public final class BukkitRewardDispatcher implements RewardDispatcher {
    private final TaskScheduler scheduler;
    private final Messages messages;

    public BukkitRewardDispatcher(TaskScheduler scheduler, Messages messages) {
        this.scheduler = scheduler;
        this.messages = messages;
    }

    @Override
    public void dispatch(Nickname nickname, List<Reward> rewards, Consumer<PendingReward> undelivered) {
        // Commands may only be dispatched on the main thread (Folia: the global region).
        scheduler.runSync(() -> {
            Player player = Bukkit.getPlayerExact(nickname.value());

            for (Reward reward : rewards) {
                if (player == null || !player.isOnline()) {
                    undelivered.accept(reward.pending());
                    continue;
                }

                Placeholders placeholders = placeholders(player, reward);

                for (RewardCommand command : reward.site().rewardCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.render(placeholders));
                }

                messages.send(player, "reward-received", placeholders);
            }
        });
    }

    static Placeholders placeholders(Player player, Reward reward) {
        return Placeholders.of("player_name", player.getName())
                .with("player_display_name", player.getDisplayName())
                .with("player_uuid", player.getUniqueId())
                .with("server_name", reward.site().name())
                .with("server_address", reward.site().address());
    }
}
