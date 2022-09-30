package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import org.bukkit.Bukkit;

public final class ThreadUtils {

    private static final RosePlugin rosePlugin = RoseStacker.getInstance();

    private ThreadUtils() {

    }

    public static void runAsync(Runnable runnable) {
        if (rosePlugin.isEnabled())
            Bukkit.getScheduler().runTaskAsynchronously(rosePlugin, runnable);
    }

    public static void runSync(Runnable runnable) {
        if (rosePlugin.isEnabled())
            Bukkit.getScheduler().runTask(rosePlugin, runnable);
    }

}
