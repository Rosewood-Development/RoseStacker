package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.ConcurrentModificationException;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PersistentDataUtils {

    private static final String UNSTACKABLE_METADATA_NAME = "unstackable";
    private static final String NO_AI_METADATA_NAME = "no_ai";
    private static final String SPAWNED_FROM_SPAWNER_METADATA_NAME = "spawner_spawned";
    private static final String SPAWNED_FROM_TRIAL_SPAWNER_METADATA_NAME = "trial_spawner_spawned";
    private static final String TOTAL_SPAWNS_METADATA_NAME = "total_spawns";

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

    public static void removeEntityAi(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME);
        if (!dataContainer.has(key, PersistentDataType.INTEGER))
            dataContainer.set(key, PersistentDataType.INTEGER, 1);

        applyDisabledAi(entity);
    }

    public static void reenableEntityAi(LivingEntity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME);
        dataContainer.remove(key);

        applyDisabledAi(entity, false);
    }

    public static void applyDisabledAi(LivingEntity entity) {
        applyDisabledAi(entity, true);
    }

    public static void applyDisabledAi(LivingEntity entity, boolean disable) {
        if (isAiDisabled(entity) || !disable) {
            if (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_REMOVE_GOALS.get() && disable) {
                NMSHandler nmsHandler = NMSAdapter.getHandler();
                nmsHandler.removeEntityGoals(entity);
            }

            if (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_DISABLE_ITEM_PICKUP.get())
                entity.setCanPickupItems(!disable);

            if (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_SET_UNAWARE.get() && entity instanceof Mob mob)
                mob.setAware(!disable);

            if (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_SILENCE.get())
                entity.setSilent(disable);

            if (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_NO_KNOCKBACK.get()) {
                AttributeInstance knockbackAttribute = entity.getAttribute(VersionUtils.KNOCKBACK_RESISTANCE);
                if (knockbackAttribute != null)
                    knockbackAttribute.setBaseValue(disable ? Double.MAX_VALUE : 0);
            }

            if (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_DISABLE_ZOMBIFICATION.get()) {
                if (entity instanceof PiglinAbstract piglin) {
                    piglin.setImmuneToZombification(disable);
                } else if (entity instanceof Hoglin hoglin) {
                    hoglin.setImmuneToZombification(disable);
                }
            }

            if (SettingKey.SPAWNER_DISABLE_MOB_AI_OPTIONS_DISABLE_COLLISION.get())
                entity.setCollidable(!disable);
        }
    }

    public static boolean isAiDisabled(LivingEntity entity) {
        EntityStackSettings entityStackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(entity.getType());
        if (entityStackSettings != null && entityStackSettings.isMobAIDisabled())
            return true;

        RosePlugin rosePlugin = RoseStacker.getInstance();
        return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, NO_AI_METADATA_NAME), PersistentDataType.INTEGER);
    }

    public static void tagSpawnedFromSpawner(Entity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, SPAWNED_FROM_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER, 1);
    }

    /**
     * Checks if an entity was spawned from a spawner
     *
     * @param entity The entity to check
     * @return true if the entity was spawned from a spawner, otherwise false
     */
    public static boolean isSpawnedFromSpawner(Entity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, SPAWNED_FROM_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER)
                || EntityUtils.hasSpawnerSpawnReason(entity);
    }

    public static void tagSpawnedFromTrialSpawner(Entity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        entity.getPersistentDataContainer().set(new NamespacedKey(rosePlugin, SPAWNED_FROM_TRIAL_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER, 1);
    }

    /**
     * Checks if an entity was spawned from a trial spawner
     *
     * @param entity The entity to check
     * @return true if the entity was spawned from a trial spawner, otherwise false
     */
    public static boolean isSpawnedFromTrialSpawner(Entity entity) {
        RosePlugin rosePlugin = RoseStacker.getInstance();
        return entity.getPersistentDataContainer().has(new NamespacedKey(rosePlugin, SPAWNED_FROM_TRIAL_SPAWNER_METADATA_NAME), PersistentDataType.INTEGER)
                || EntityUtils.hasTrialSpawnerSpawnReason(entity);
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
        try {
            RosePlugin rosePlugin = RoseStacker.getInstance();
            PersistentDataContainer persistentDataContainer = spawner.getPersistentDataContainer();
            if (persistentDataContainer == null)
                return 0;

            NamespacedKey key = new NamespacedKey(rosePlugin, TOTAL_SPAWNS_METADATA_NAME);
            Long amount = persistentDataContainer.get(key, PersistentDataType.LONG);
            return amount != null ? amount : 0;
        } catch (ConcurrentModificationException e) {
            return 0; // StackedSpawner#updateDisplay can cause a CME sometimes here when run async
        }
    }

}
