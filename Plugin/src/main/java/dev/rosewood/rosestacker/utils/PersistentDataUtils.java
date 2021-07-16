package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PersistentDataUtils {

    private static final String UNSTACKABLE_METADATA_NAME = "unstackable";
    private static final String SPAWN_REASON_METADATA_NAME = "spawn_reason";
    private static final String NO_AI_METADATA_NAME = "no_ai";
    private static final String SPAWNED_FROM_SPAWNER_METADATA_NAME = "spawner_spawned";
    private static final String TOTAL_SPAWNS_METADATA_NAME = "total_spawns";
    private static final NamespacedKey MIGRATED_KEY = new NamespacedKey(RoseStacker.getInstance(), "chunk_migrated");
    public static final NamespacedKey CONVERTED_KEY = new NamespacedKey(RoseStacker.getInstance(), "converted");

    public static boolean isChunkConverted(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        return pdc.has(CONVERTED_KEY, PersistentDataType.INTEGER);
    }

    public static void setChunkConverted(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        pdc.set(CONVERTED_KEY, PersistentDataType.INTEGER, 1);
    }

    public static boolean isChunkMigrated(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        return pdc.has(MIGRATED_KEY, PersistentDataType.INTEGER);
    }

    public static void setChunkMigrated(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        pdc.set(MIGRATED_KEY, PersistentDataType.INTEGER, 1);
    }

    public static void setUnstackable(LivingEntity entity, boolean unstackable) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (unstackable) {
            entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME), PersistentDataType.INTEGER, 1);
        } else {
            entity.getPersistentDataContainer().remove(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME));
        }
    }

    public static boolean isUnstackable(LivingEntity entity) {
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
    public static void setEntitySpawnReason(LivingEntity entity, SpawnReason spawnReason) {
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
    public static SpawnReason getEntitySpawnReason(LivingEntity entity) {
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
        if (isAiDisabled(entity)) {
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            nmsHandler.removeEntityGoals(entity);
            entity.setSilent(true);

            // Make the entity unable to take knockback
            AttributeInstance knockbackAttribute = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            if (knockbackAttribute != null)
                knockbackAttribute.setBaseValue(Double.MAX_VALUE);
        }
    }

    public static boolean isAiDisabled(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME), PersistentDataType.INTEGER);
    }

    public static void tagSpawnedFromSpawner(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, SPAWNED_FROM_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER, 1);
    }

    /**
     * Checks if an entity was spawned from one of our spawners
     *
     * @param entity The entity to check
     * @return true if the entity was spawned from one of our spawners, otherwise false
     */
    public static boolean isSpawnedFromSpawner(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, SPAWNED_FROM_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER);
    }

    public static void increaseSpawnCount(CreatureSpawner spawner, long amount) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        PersistentDataContainer dataContainer = spawner.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(rosePlugin, TOTAL_SPAWNS_METADATA_NAME);
        if (!dataContainer.has(key, PersistentDataType.LONG)) {
            dataContainer.set(key, PersistentDataType.LONG, amount);
        } else {
            dataContainer.set(key, PersistentDataType.LONG, getTotalSpawnCount(spawner) + amount);
        }
        spawner.update();
    }

    public static long getTotalSpawnCount(CreatureSpawner spawner) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        PersistentDataContainer persistentDataContainer = spawner.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(rosePlugin, TOTAL_SPAWNS_METADATA_NAME);
        Long amount = persistentDataContainer.get(key, PersistentDataType.LONG);
        return amount != null ? amount : 0;
    }

}
