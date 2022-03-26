package dev.rosewood.rosestacker.hook;

import org.bukkit.entity.LivingEntity;

public interface MythicMobsHook {

    /**
     * Checks if the given entity is a MythicMob
     *
     * @param entity the entity to check
     * @return true if the entity is a MythicMob, false otherwise
     */
    boolean isMythicMob(LivingEntity entity);

}
