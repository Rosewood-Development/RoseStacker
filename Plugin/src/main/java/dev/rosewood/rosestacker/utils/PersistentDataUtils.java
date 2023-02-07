package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PersistentDataUtils {

    private static final String UNSTACKABLE_METADATA_NAME = "unstackable";
    private static final String SPAWN_REASON_METADATA_NAME = "spawn_reason";
    private static final String NO_AI_METADATA_NAME = "no_ai";
    private static final String SPAWNED_FROM_SPAWNER_METADATA_NAME = "spawner_spawned";
    private static final String TOTAL_SPAWNS_METADATA_NAME = "total_spawns";
    public static final NamespacedKey CONVERTED_KEY = new NamespacedKey(RoseStacker.getInstance(), "converted");

    public static boolean isChunkConverted(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        return pdc.has(CONVERTED_KEY, PersistentDataType.INTEGER);
    }

    public static void setChunkConverted(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        pdc.set(CONVERTED_KEY, PersistentDataType.INTEGER, 1);
    }

    public static void setUnstackable(Entity entity, boolean unstackable) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (unstackable) {
            entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME), PersistentDataType.INTEGER, 1);
        } else {
            entity.getPersistentDataContainer().remove(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME));
        }
    }

    public static boolean isUnstackable(Entity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME), PersistentDataType.INTEGER);
    }

    /**
     * Sets the spawn reason for the given LivingEntity.
     * Does not overwrite an existing spawn reason.
     *
     * @param entity The entity to set the spawn reason of
     * @param spawnReason The spawn reason to set
     */
    public static void setEntitySpawnReason(Entity entity, SpawnReason spawnReason) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME);
        if (!dataContainer.has(key, PersistentDataType.STRING))
            dataContainer.set(key, PersistentDataType.STRING, spawnReason.name());
    }

    /**
     * Gets the spawn reason of the given LivingEntity
     *
     * @param entity The entity to get the spawn reason of
     * @return The SpawnReason, or SpawnReason.CUSTOM if none is saved
     */
    public static SpawnReason getEntitySpawnReason(Entity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        String reason = entity.getPersistentDataContainer().get(new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME), PersistentDataType.STRING);
        SpawnReason spawnReason;
        if (reason != null) {
            try {
                spawnReason = SpawnReason.valueOf(reason);
            } catch (Exception ex) {
                spawnReason = SpawnReason.CUSTOM;
            }
        } else {
            spawnReason = SpawnReason.CUSTOM;
        }
        return spawnReason;
    }

    public static void removeEntityAi(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME);
        if (!dataContainer.has(key, PersistentDataType.INTEGER))
            dataContainer.set(key, PersistentDataType.INTEGER, 1);

        applyDisabledAi(entity);
    }

    public static void applyDisabledAi(LivingEntity entity) {
        if (isAiDisabled(entity) || Setting.ENTITY_DISABLE_ALL_MOB_AI.getBoolean()) {
            if (Setting.SPAWNER_DISABLE_MOB_AI_OPTIONS_REMOVE_GOALS.getBoolean()) {
                NMSHandler nmsHandler = NMSAdapter.getHandler();
                nmsHandler.removeEntityGoals(entity);
            }

            if (Setting.SPAWNER_DISABLE_MOB_AI_OPTIONS_SILENCE.getBoolean())
                entity.setSilent(true);

            if (Setting.SPAWNER_DISABLE_MOB_AI_OPTIONS_NO_KNOCKBACK.getBoolean()) {
                // Make the entity unable to take knockback
                AttributeInstance knockbackAttribute = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                if (knockbackAttribute != null)
                    knockbackAttribute.setBaseValue(Double.MAX_VALUE);
            }

            if (Setting.SPAWNER_DISABLE_MOB_AI_OPTIONS_DISABLE_ZOMBIFICATION.getBoolean()) {
                if (entity instanceof PiglinAbstract) {
                    ((PiglinAbstract) entity).setImmuneToZombification(true);
                } else if (entity instanceof Hoglin) {
                    ((Hoglin) entity).setImmuneToZombification(true);
                }
            }
        }
    }

    public static boolean isAiDisabled(LivingEntity entity) {
        if (Setting.ENTITY_DISABLE_ALL_MOB_AI.getBoolean())
            return true;

        RosePlugin rosePlugin = RoseStacker.getInstance();
        return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME), PersistentDataType.INTEGER);
    }

    public static void tagSpawnedFromSpawner(Entity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, SPAWNED_FROM_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER, 1);
    }

    /**
     * Checks if an entity was spawned from one of our spawners
     *
     * @param entity The entity to check
     * @return true if the entity was spawned from one of our spawners, otherwise false
     */
    public static boolean isSpawnedFromSpawner(Entity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, SPAWNED_FROM_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER);
    }

    public static void increaseSpawnCount(StackedSpawnerTile spawner, long amount) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        PersistentDataContainer dataContainer = spawner.getPersistentDataContainer();
        if (dataContainer != null) {
            NamespacedKey key = new NamespacedKey(rosePlugin, TOTAL_SPAWNS_METADATA_NAME);
            if (!dataContainer.has(key, PersistentDataType.LONG)) {
                dataContainer.set(key, PersistentDataType.LONG, amount);
            } else {
                dataContainer.set(key, PersistentDataType.LONG, getTotalSpawnCount(spawner) + amount);
            }
        }
    }

    public static long getTotalSpawnCount(StackedSpawnerTile spawner) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        PersistentDataContainer persistentDataContainer = spawner.getPersistentDataContainer();
        if (persistentDataContainer == null)
            return 0;

        NamespacedKey key = new NamespacedKey(rosePlugin, TOTAL_SPAWNS_METADATA_NAME);
        Long amount = persistentDataContainer.get(key, PersistentDataType.LONG);
        return amount != null ? amount : 0;
    }

}
