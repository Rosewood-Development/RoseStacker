package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.scheduler.RoseScheduler;
import dev.rosewood.rosestacker.RoseStacker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class ThreadUtils {

    private static final RosePlugin PLUGIN = RoseStacker.getInstance();
    private static final RoseScheduler SCHEDULER = PLUGIN.getScheduler();

    private ThreadUtils() {

    }

    /**
     * Runs on the primary thread, inline if already there. On Folia there is no single primary thread, so this
     * only guarantees a tick thread; use the entity/location overloads when the task touches world state.
     */
    public static void runOnPrimary(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            runSync(runnable);
        }
    }

    public static void runOffPrimary(Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            runAsync(runnable);
        }
    }

    /**
     * Next-tick task that does not touch a specific entity or location (global region on Folia).
     */
    public static void runSyncGlobal(Runnable runnable) {
        if (checkEnabled())
            SCHEDULER.runTask(runnable);
    }

    public static void runSyncDelayedGlobal(Runnable runnable, long delay) {
        if (checkEnabled())
            SCHEDULER.runTaskLater(runnable, delay);
    }

    /**
     * @deprecated no owner given; kept for source compatibility. Use the {@link Entity}/{@link Location} overloads.
     */
    @Deprecated
    public static void runSync(Runnable runnable) {
        runSyncGlobal(runnable);
    }

    /**
     * @deprecated no owner given; kept for source compatibility. Use the {@link Entity}/{@link Location} overloads.
     */
    @Deprecated
    public static void runSyncDelayed(Runnable runnable, long delay) {
        runSyncDelayedGlobal(runnable, delay);
    }

    /**
     * Next-tick task that touches the given entity; runs on the entity's owning thread.
     */
    public static void runSync(Entity owner, Runnable runnable) {
        if (checkEnabled())
            Folia.runNextTick(owner, runnable, 0L);
    }

    public static void runSyncDelayed(Entity owner, Runnable runnable, long delay) {
        if (checkEnabled())
            Folia.runNextTick(owner, runnable, delay);
    }

    /**
     * Next-tick task that touches world state at the location; runs on the owning region.
     */
    public static void runSync(Location owner, Runnable runnable) {
        if (checkEnabled())
            Folia.runNextTick(owner, runnable, 0L);
    }

    public static void runSyncDelayed(Location owner, Runnable runnable, long delay) {
        if (checkEnabled())
            Folia.runNextTick(owner, runnable, delay);
    }

    /**
     * Runs inline when the caller owns the entity, otherwise hops to its owner.
     */
    public static void runOnOwner(Entity owner, Runnable runnable) {
        if (checkEnabled())
            Folia.run(owner, runnable);
    }

    public static void runOnOwner(Location owner, Runnable runnable) {
        if (checkEnabled())
            Folia.run(owner, runnable);
    }

    /**
     * Async on classic servers; on Folia the work touches entities, so it runs on the entity's owner instead.
     */
    public static void runAsync(Entity owner, Runnable runnable) {
        if (Folia.ENABLED) {
            runOnOwner(owner, runnable);
        } else {
            runAsync(runnable);
        }
    }

    public static void runAsync(Location owner, Runnable runnable) {
        if (Folia.ENABLED) {
            runOnOwner(owner, runnable);
        } else {
            runAsync(runnable);
        }
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
