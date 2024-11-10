package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.scheduler.RoseScheduler;
import dev.rosewood.rosestacker.RoseStacker;
import org.bukkit.Bukkit;

public final class ThreadUtils {

    private static final RosePlugin PLUGIN = RoseStacker.getInstance();
    private static final RoseScheduler SCHEDULER = PLUGIN.getScheduler();

    private ThreadUtils() {

    }

    public static void runOnPrimary(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            runSync(runnable);
        }
    }

    public static void runSync(Runnable runnable) {
        if (checkEnabled())
            SCHEDULER.runTask(runnable);
    }

    public static void runSyncDelayed(Runnable runnable, long delay) {
        if (checkEnabled())
            SCHEDULER.runTaskLater(runnable, delay);
    }

    public static void runAsync(Runnable runnable) {
        if (checkEnabled())
            SCHEDULER.runTaskAsync(runnable);
    }

    public static void runAsyncDelayed(Runnable runnable, long delay) {
        if (checkEnabled())
            SCHEDULER.runTaskLaterAsync(runnable, delay);
    }

    public static int getActiveThreads() {
        return SCHEDULER.getRunningTaskCount();
    }

    private static boolean checkEnabled() {
        return PLUGIN.isEnabled();
    }

}
