package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosestacker.RoseStacker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Single boundary between RoseStacker and Folia's region threading model.
 * <p>
 * All Folia-only API (entity scheduler, region scheduler, ownership checks) is isolated in {@link Impl},
 * which is never loaded on non-Folia servers, so legacy Spigot/Paper classloading stays safe.
 * On non-Folia servers every method here degrades to the classic "primary thread" behavior.
 */
public final class Folia {

    /**
     * True when running on a Folia (regionized) server. Decided once at class load.
     */
    public static final boolean ENABLED = detect();

    private Folia() {

    }

    private static boolean detect() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * @return true if the current thread may safely touch the given entity
     */
    public static boolean isOwner(Entity entity) {
        return ENABLED ? Impl.owns(entity) : Bukkit.isPrimaryThread();
    }

    /**
     * @return true if the current thread may safely touch the world state at the given location
     */
    public static boolean isOwner(Location location) {
        if (!ENABLED)
            return Bukkit.isPrimaryThread();
        return location.getWorld() != null && Impl.owns(location);
    }

    /**
     * @return true if the current thread owns the chunk square of the given radius (in chunks) around the location
     */
    public static boolean isOwner(Location location, int chunkRadius) {
        if (!ENABLED)
            return Bukkit.isPrimaryThread();
        return location.getWorld() != null && Impl.owns(location, chunkRadius);
    }

    /**
     * Runs the task on the thread owning the entity. Runs inline if the caller already owns it.
     * Silently dropped if the entity is removed/retired before the task can run.
     */
    public static void run(Entity entity, Runnable task) {
        if (!ENABLED) {
            ThreadUtils.runOnPrimary(task);
        } else if (Impl.owns(entity)) {
            task.run();
        } else {
            Impl.schedule(entity, task, 0L);
        }
    }

    /**
     * Runs the task on the entity's owner on the next tick (or later), never inline.
     */
    public static void runNextTick(Entity entity, Runnable task, long delayTicks) {
        if (!ENABLED) {
            if (delayTicks <= 0) {
                ThreadUtils.runSyncGlobal(task);
            } else {
                ThreadUtils.runSyncDelayedGlobal(task, delayTicks);
            }
        } else {
            Impl.schedule(entity, task, Math.max(1L, delayTicks));
        }
    }

    /**
     * Runs the task on the region owning the location. Runs inline if the caller already owns it.
     */
    public static void run(Location location, Runnable task) {
        if (!ENABLED) {
            ThreadUtils.runOnPrimary(task);
        } else if (location.getWorld() != null && Impl.owns(location)) {
            task.run();
        } else {
            Impl.schedule(location, task, 0L);
        }
    }

    /**
     * Runs the task on the region owning the location on the next tick (or later), never inline.
     */
    public static void runNextTick(Location location, Runnable task, long delayTicks) {
        if (!ENABLED) {
            if (delayTicks <= 0) {
                ThreadUtils.runSyncGlobal(task);
            } else {
                ThreadUtils.runSyncDelayedGlobal(task, delayTicks);
            }
        } else {
            Impl.schedule(location, task, Math.max(1L, delayTicks));
        }
    }

    /**
     * Runs the task on the region owning the given chunk.
     */
    public static void runChunk(World world, int chunkX, int chunkZ, Runnable task) {
        if (!ENABLED) {
            ThreadUtils.runOnPrimary(task);
        } else {
            Impl.scheduleChunk(world, chunkX, chunkZ, task);
        }
    }

    private static final class Impl {

        static boolean owns(Entity entity) {
            return Bukkit.isOwnedByCurrentRegion(entity);
        }

        static boolean owns(Location location) {
            return Bukkit.isOwnedByCurrentRegion(location);
        }

        static boolean owns(Location location, int chunkRadius) {
            return Bukkit.isOwnedByCurrentRegion(location, chunkRadius);
        }

        static void schedule(Entity entity, Runnable task, long delay) {
            Plugin plugin = RoseStacker.getInstance();
            if (!plugin.isEnabled())
                return;

            // retired callback is null: if the entity is removed before the task runs, the task is simply dropped
            if (delay <= 0) {
                entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
            } else {
                entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delay);
            }
        }

        static void schedule(Location location, Runnable task, long delay) {
            Plugin plugin = RoseStacker.getInstance();
            if (!plugin.isEnabled())
                return;

            if (delay <= 0) {
                Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
            } else {
                Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay);
            }
        }

        static void scheduleChunk(World world, int chunkX, int chunkZ, Runnable task) {
            Plugin plugin = RoseStacker.getInstance();
            if (!plugin.isEnabled())
                return;

            Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, scheduledTask -> task.run());
        }

    }

}
