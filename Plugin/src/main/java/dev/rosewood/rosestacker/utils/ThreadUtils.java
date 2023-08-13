package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
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
        return () -> {
            activeThreads.incrementAndGet();
            try {
                runnable.run();
            } finally {
                activeThreads.decrementAndGet();
            }
        };
    }

    private static boolean checkEnabled() {
        return rosePlugin.isEnabled();
    }

}
