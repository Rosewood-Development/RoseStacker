package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PersistentDataUtils {

    private static final String UNSTACKABLE_METADATA_NAME = "unstackable";
    private static final String SPAWN_REASON_METADATA_NAME = "spawn_reason";
    private static final String NO_AI_METADATA_NAME = "no_ai";
    private static final String SPAWNED_FROM_SPAWNER_METADATA_NAME = "spawner_spawned";
    private static final String TOTAL_SPAWNS_METADATA_NAME = "total_spawns";

    public static void setUnstackable(LivingEntity entity, boolean unstackable) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (unstackable) {
            if (NMSUtil.getVersionNumber() > 13) {
                entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME), PersistentDataType.INTEGER, 1);
            } else {
                entity.setMetadata(UNSTACKABLE_METADATA_NAME, new FixedMetadataValue(rosePlugin, true));
            }
        } else {
            if (NMSUtil.getVersionNumber() > 13) {
                entity.getPersistentDataContainer().remove(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME));
            } else {
                entity.removeMetadata(UNSTACKABLE_METADATA_NAME, rosePlugin);
            }
        }
    }

    public static boolean isUnstackable(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, UNSTACKABLE_METADATA_NAME), PersistentDataType.INTEGER);
        } else {
            return entity.hasMetadata(UNSTACKABLE_METADATA_NAME);
        }
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
        if (NMSUtil.getVersionNumber() > 13) {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME);
            if (!dataContainer.has(key, PersistentDataType.STRING))
                dataContainer.set(key, PersistentDataType.STRING, spawnReason.name());
        } else {
            if (!entity.hasMetadata(SPAWN_REASON_METADATA_NAME))
                entity.setMetadata(SPAWN_REASON_METADATA_NAME, new FixedMetadataValue(rosePlugin, spawnReason.name()));
        }
    }

    /**
     * Gets the spawn reason of the given LivingEntity
     *
     * @param entity The entity to get the spawn reason of
     * @return The SpawnReason, or SpawnReason.CUSTOM if none is saved
     */
    public static SpawnReason getEntitySpawnReason(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
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
        } else {
            List<MetadataValue> metaValues = entity.getMetadata(SPAWN_REASON_METADATA_NAME);
            SpawnReason spawnReason = null;
            for (MetadataValue meta : metaValues) {
                try {
                    spawnReason = SpawnReason.valueOf(meta.asString());
                    break;
                } catch (Exception ignored) { }
            }
            return spawnReason != null ? spawnReason : SpawnReason.CUSTOM;
        }
    }

    public static void removeEntityAi(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME);
            if (!dataContainer.has(key, PersistentDataType.INTEGER))
                dataContainer.set(key, PersistentDataType.INTEGER, 1);
        } else {
            if (!entity.hasMetadata(NO_AI_METADATA_NAME))
                entity.setMetadata(NO_AI_METADATA_NAME, new FixedMetadataValue(rosePlugin, true));
        }

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
        boolean isDisabled;
        if (NMSUtil.getVersionNumber() > 13) {
            isDisabled = entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME), PersistentDataType.INTEGER);
        } else {
            isDisabled = entity.hasMetadata(NO_AI_METADATA_NAME);
        }

        return isDisabled;
    }

    public static void tagSpawnedFromSpawner(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, SPAWNED_FROM_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER, 1);
        } else {
            entity.setMetadata(SPAWNED_FROM_SPAWNER_METADATA_NAME, new FixedMetadataValue(rosePlugin, true));
        }
    }

    /**
     * Checks if an entity was spawned from one of our spawners
     *
     * @param entity The entity to check
     * @return true if the entity was spawned from one of our spawners, otherwise false
     */
    public static boolean isSpawnedFromSpawner(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, SPAWNED_FROM_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER);
        } else {
            return entity.hasMetadata(SPAWNED_FROM_SPAWNER_METADATA_NAME);
        }
    }

    public static void increaseSpawnCount(CreatureSpawner spawner, long amount) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            PersistentDataContainer dataContainer = spawner.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(rosePlugin, TOTAL_SPAWNS_METADATA_NAME);
            if (!dataContainer.has(key, PersistentDataType.LONG)) {
                dataContainer.set(key, PersistentDataType.LONG, amount);
            } else {
                dataContainer.set(key, PersistentDataType.LONG, getTotalSpawnCount(spawner) + amount);
            }
        } else {
            if (!spawner.hasMetadata(TOTAL_SPAWNS_METADATA_NAME)) {
                spawner.setMetadata(TOTAL_SPAWNS_METADATA_NAME, new FixedMetadataValue(rosePlugin, amount));
            } else {
                spawner.setMetadata(TOTAL_SPAWNS_METADATA_NAME, new FixedMetadataValue(rosePlugin, getTotalSpawnCount(spawner) + amount));
            }
        }
        spawner.update();
    }

    public static long getTotalSpawnCount(CreatureSpawner spawner) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            PersistentDataContainer persistentDataContainer = spawner.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(rosePlugin, TOTAL_SPAWNS_METADATA_NAME);
            Long amount = persistentDataContainer.get(key, PersistentDataType.LONG);
            return amount != null ? amount : 0;
        } else {
            List<MetadataValue> metaValues = spawner.getMetadata(TOTAL_SPAWNS_METADATA_NAME);
            long amount = 0;
            for (MetadataValue meta : metaValues) {
                try {
                    amount = meta.asLong();
                    break;
                } catch (Exception ignored) { }
            }
            return amount;
        }
    }

}
