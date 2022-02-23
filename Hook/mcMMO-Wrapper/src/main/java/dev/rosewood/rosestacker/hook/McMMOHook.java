package dev.rosewood.rosestacker.hook;

import org.bukkit.entity.LivingEntity;

public interface McMMOHook {

    /**
     * Flags a LivingEntity as having been spawned from a spawner
     *
     * @param entity The LivingEntity to flag
     */
    void flagSpawnerMetadata(LivingEntity entity);

}
