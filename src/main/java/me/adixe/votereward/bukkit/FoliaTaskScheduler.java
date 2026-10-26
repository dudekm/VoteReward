package me.adixe.votereward.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * {@link TaskScheduler} for Folia. Its scheduler API is not part of the Spigot API the
 * plugin is compiled against, so it is reached through reflection.
 */
final class FoliaTaskScheduler implements TaskScheduler {
    private static final String REGIONIZED_SERVER = "io.papermc.paper.threadedregions.RegionizedServer";
    private static final String ASYNC_SCHEDULER = "io.papermc.paper.threadedregions.scheduler.AsyncScheduler";
    private static final String GLOBAL_SCHEDULER = "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler";

    private final Plugin plugin;
    private final Object asyncScheduler;
    private final Object globalScheduler;
    private final Method runNow;
    private final Method runAtFixedRate;
    private final Method cancelTasks;
    private final Method execute;

    FoliaTaskScheduler(Plugin plugin) {
        this.plugin = plugin;

        try {
            Class<?> asyncType = Class.forName(ASYNC_SCHEDULER);
            Class<?> globalType = Class.forName(GLOBAL_SCHEDULER);

            this.asyncScheduler = Bukkit.getServer().getClass().getMethod("getAsyncScheduler").invoke(Bukkit.getServer());
            this.globalScheduler = Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler")
                    .invoke(Bukkit.getServer());
            this.runNow = asyncType.getMethod("runNow", Plugin.class, Consumer.class);
            this.runAtFixedRate = asyncType.getMethod("runAtFixedRate", Plugin.class, Consumer.class,
                    long.class, long.class, TimeUnit.class);
            this.cancelTasks = asyncType.getMethod("cancelTasks", Plugin.class);
            this.execute = globalType.getMethod("execute", Plugin.class, Runnable.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Folia detected, but its scheduler API could not be found.", exception);
        }
    }

    static boolean isFolia() {
        try {
            Class.forName(REGIONIZED_SERVER);

            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    @Override
    public void runAsync(Runnable task) {
        invoke(runNow, asyncScheduler, plugin, (Consumer<Object>) scheduled -> task.run());
    }

    @Override
    public void runAsyncRepeating(Runnable task, Duration period) {
        long millis = Math.max(1, period.toMillis());
        invoke(runAtFixedRate, asyncScheduler, plugin, (Consumer<Object>) scheduled -> task.run(),
                millis, millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runSync(Runnable task) {
        invoke(execute, globalScheduler, plugin, task);
    }

    @Override
    public void cancelAll() {
        invoke(cancelTasks, asyncScheduler, plugin);
        // Global-region tasks are one-shot here and finish on their own.
    }

    private static void invoke(Method method, Object target, Object... arguments) {
        try {
            method.invoke(target, arguments);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Folia scheduler call " + method.getName() + " failed.", exception);
        }
    }
}
