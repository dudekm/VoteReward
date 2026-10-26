package me.adixe.votereward.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

/**
 * {@link TaskScheduler} for CraftBukkit, Spigot, Paper, Purpur and their forks.
 */
final class BukkitTaskScheduler implements TaskScheduler {
    private static final long MILLIS_PER_TICK = 50;

    private final Plugin plugin;

    BukkitTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runAsyncRepeating(Runnable task, Duration period) {
        long ticks = Math.max(1, period.toMillis() / MILLIS_PER_TICK);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, ticks, ticks);
    }

    @Override
    public void runSync(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    @Override
    public void cancelAll() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
