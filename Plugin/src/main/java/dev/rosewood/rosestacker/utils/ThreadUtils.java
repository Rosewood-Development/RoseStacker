package dev.rosewood.rosestacker.utils;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;

public final class ThreadUtils {

    private static final boolean DEBUG = false;

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
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                runnable.run();
            } finally {
                activeThreads.decrementAndGet();
                if (DEBUG) {
                    double ms = stopwatch.elapsed().toNanos() / 1000000.0;
                    if (ms > 0)
                        rosePlugin.getLogger().warning("Thread took " + DecimalFormat.getInstance().format(ms) + "ms to complete");
                }
            }
        };
    }

    private static boolean checkEnabled() {
        if (!rosePlugin.isEnabled()) {
            if (DEBUG)
                rosePlugin.getLogger().warning(String.format("(%d) [%s] Attempted to run a task while the plugin was disabled", activeThreads.get(), getCallingMethod(5)));
            return false;
        }

        if (DEBUG)
            rosePlugin.getLogger().warning(String.format("(%d) [%s] -> %s", activeThreads.get(), getCallingMethod(5), getCallingTaskType(4)));

        return true;
    }

    private static String getCallingMethod(int depth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < depth)
            return "[Unknown]";
        String className = stackTrace[depth - 1].getClassName();
        return String.format("%s::%s", className.substring(className.lastIndexOf('.') + 1), stackTrace[depth - 1].getMethodName());
    }

    private static String getCallingTaskType(int depth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < depth)
            return "[Unknown]";
        return stackTrace[depth - 1].getMethodName();
    }

}
