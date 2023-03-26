package dev.rosewood.rosestacker.utils;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;

public final class ThreadUtils {

    private static final AtomicInteger activeThreads = new AtomicInteger(0);
    private static final RosePlugin rosePlugin = RoseStacker.getInstance();

    private ThreadUtils() {

    }

    public static void runSync(Runnable runnable) {
        if (checkEnabled())
            Bukkit.getScheduler().runTask(rosePlugin, wrap(runnable));
    }

    public static void runSyncDelayed(Runnable runnable, long delay) {
        if (checkEnabled())
            Bukkit.getScheduler().runTaskLater(rosePlugin, wrap(runnable), delay);
    }

    public static void runAsync(Runnable runnable) {
        if (checkEnabled())
            Bukkit.getScheduler().runTaskAsynchronously(rosePlugin, wrap(runnable));
    }

    public static void runAsyncDelayed(Runnable runnable, long delay) {
        if (checkEnabled())
            Bukkit.getScheduler().runTaskLaterAsynchronously(rosePlugin, wrap(runnable), delay);
    }

    public static int getActiveThreads() {
        return activeThreads.get();
    }

    private static Runnable wrap(Runnable runnable) {
        if (Debug.isLoggingEnabled()) {
            return () -> {
                activeThreads.incrementAndGet();
                Stopwatch stopwatch = Stopwatch.createStarted();
                try {
                    runnable.run();
                } finally {
                    activeThreads.decrementAndGet();
                    double ms = stopwatch.elapsed().toNanos() / 1000000.0;
                    if (ms > Setting.DEBUG_LOGGING_THREAD_DURATION_THRESHOLD.getDouble())
                        Debug.log("Thread took " + DecimalFormat.getInstance().format(ms) + "ms to complete");
                }
            };
        } else {
            return () -> {
                activeThreads.incrementAndGet();
                try {
                    runnable.run();
                } finally {
                    activeThreads.decrementAndGet();
                }
            };
        }
    }

    private static boolean checkEnabled() {
        if (!rosePlugin.isEnabled()) {
            Debug.log(Setting.DEBUG_LOGGING_THREAD_DISABLED_WARNING::getBoolean, () -> String.format("(%d) Attempted to run a task while the plugin was disabled", activeThreads.get()));
            return false;
        }

        return true;
    }

}
