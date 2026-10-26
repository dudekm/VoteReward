package me.adixe.votereward.bukkit;

import java.time.Duration;

/**
 * Runs plugin tasks. Folia has no main thread and rejects the classic Bukkit scheduler,
 * so the plugin goes through this interface and picks the implementation at startup.
 */
public interface TaskScheduler {
    /** Off the main thread — for HTTP calls and file writes. */
    void runAsync(Runnable task);

    /** Off the main thread, every {@code period}, the first time after {@code period}. */
    void runAsyncRepeating(Runnable task, Duration period);

    /** On the thread allowed to run console commands: the main thread, or Folia's global region. */
    void runSync(Runnable task);

    void cancelAll();

    static TaskScheduler detect(org.bukkit.plugin.Plugin plugin) {
        return FoliaTaskScheduler.isFolia() ? new FoliaTaskScheduler(plugin) : new BukkitTaskScheduler(plugin);
    }
}
