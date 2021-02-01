package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class EntityDataUtils {

    private static final String UNSTACKABLE_METADATA_NAME = "unstackable";
    private static final String SPAWN_REASON_METADATA_NAME = "spawn_reason";
    private static final String NO_AI_METADATA_NAME = "no_ai";

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
    public static void setEntitySpawnReason(LivingEntity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
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
    public static CreatureSpawnEvent.SpawnReason getEntitySpawnReason(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            String reason = entity.getPersistentDataContainer().get(new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME), PersistentDataType.STRING);
            CreatureSpawnEvent.SpawnReason spawnReason;
            if (reason != null) {
                try {
                    spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reason);
                } catch (Exception ex) {
                    spawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;
                }
            } else {
                spawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM;
            }
            return spawnReason;
        } else {
            List<MetadataValue> metaValues = entity.getMetadata(SPAWN_REASON_METADATA_NAME);
            CreatureSpawnEvent.SpawnReason spawnReason = null;
            for (MetadataValue meta : metaValues) {
                try {
                    spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(meta.asString());
                } catch (Exception ignored) { }
            }
            return spawnReason != null ? spawnReason : CreatureSpawnEvent.SpawnReason.CUSTOM;
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

}
