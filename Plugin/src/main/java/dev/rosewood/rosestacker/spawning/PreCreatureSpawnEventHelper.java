package dev.rosewood.rosestacker.spawning;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Separate class due to classloader issues when running on Spigot
 */
public final class PreCreatureSpawnEventHelper {

    private PreCreatureSpawnEventHelper() {

    }

    public static PreCreatureSpawnEventResult call(Location location, EntityType entityType, CreatureSpawnEvent.SpawnReason spawnReason) {
        PreCreatureSpawnEvent preCreatureSpawnEvent = new PreCreatureSpawnEvent(location, entityType, spawnReason);
        Bukkit.getPluginManager().callEvent(preCreatureSpawnEvent);
        return new PreCreatureSpawnEventResult(preCreatureSpawnEvent.shouldAbortSpawn(), preCreatureSpawnEvent.isCancelled());
    }

    public record PreCreatureSpawnEventResult(boolean abort, boolean cancel) { }

}
