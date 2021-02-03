package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.nms.object.SpawnerTileWrapper;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.NoneConditionTag;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public class SpawnerSpawnManager extends Manager implements Runnable {

    /**
     * Metadata name used to keep track of if an entity is spawned from a spawner
     */
    private static final String METADATA_NAME = "spawner_spawned";

    /**
     * At what point should we override the normal spawner spawning?
     */
    public static final int DELAY_THRESHOLD = 3;

    private final Random random;
    private BukkitTask task;

    public SpawnerSpawnManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.random = new Random();
    }

    @Override
    public void reload() {
        if (!this.rosePlugin.getManager(StackManager.class).isSpawnerStackingEnabled())
            return;

        this.task = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, this, 0, 1);
    }

    @Override
    public void disable() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    @Override
    public void run() {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        StackSettingManager stackSettingManager = this.rosePlugin.getManager(StackSettingManager.class);

        boolean randomizeSpawnAmounts = Setting.SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED.getBoolean();
        int maxFailedSpawnAttempts = Setting.SPAWNER_MAX_FAILED_SPAWN_ATTEMPTS.getInt();
        boolean redstoneSpawners = Setting.SPAWNER_DEACTIVATE_WHEN_POWERED.getBoolean();

        Map<Block, StackedSpawner> stackedSpawners = stackManager.getStackedSpawners();
        for (Entry<Block, StackedSpawner> entry : stackedSpawners.entrySet()) {
            Block block = entry.getKey();

            // Make sure the chunk is loaded
            if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4))
                continue;

            // Make sure it's still a spawner
            if (block.getType() != Material.SPAWNER)
                continue;

            StackedSpawner stackedSpawner = entry.getValue();
            SpawnerStackSettings stackSettings = stackedSpawner.getStackSettings();
            SpawnerTileWrapper spawnerTile = stackedSpawner.getSpawnerTile();
            CreatureSpawner spawner = stackedSpawner.getSpawner();
            if (redstoneSpawners) {
                boolean isPowered = block.isBlockPowered();
                boolean wasPowered = stackedSpawner.isPowered();
                boolean changed = false;
                if (isPowered && !wasPowered) {
                    // Prevent the spawner from spinning and counting down the delay (in most cases)
                    spawnerTile.setRequiredPlayerRange(1);
                    changed = true;
                } else if (!isPowered && wasPowered) {
                    spawnerTile.setRequiredPlayerRange(stackSettings.getPlayerActivationRange());
                    changed = true;
                }

                int delay = spawnerTile.getDelay();
                if (isPowered) {
                    int lastDelay = stackedSpawner.getLastDelay();

                    // If the spawner is still spinning, prevent it from counting down
                    if (lastDelay != delay) {
                        spawnerTile.setDelay(lastDelay);
                        changed = true;
                    }
                }

                if (changed) {
                    stackedSpawner.setPowered(isPowered);
                }
            }

            stackedSpawner.setLastDelay(spawnerTile.getDelay());
            if (stackedSpawner.getLastDelay() > DELAY_THRESHOLD)
                continue;

            EntityType entityType = spawner.getSpawnedType();
            if (entityType.getEntityClass() == null || !LivingEntity.class.isAssignableFrom(entityType.getEntityClass()))
                continue;

            // Reset the spawn delay
            int newDelay = this.random.nextInt(spawnerTile.getMaxSpawnDelay() - spawnerTile.getMinSpawnDelay() + 1) + spawnerTile.getMinSpawnDelay();
            stackedSpawner.setLastDelay(newDelay);
            spawnerTile.setDelay(newDelay);

            List<ConditionTag> spawnRequirements = new ArrayList<>(stackSettings.getSpawnRequirements());

            // Check general spawner conditions
            List<ConditionTag> perSpawnConditions = spawnRequirements.stream().filter(ConditionTag::isRequiredPerSpawn).collect(Collectors.toList());
            spawnRequirements.removeAll(perSpawnConditions);

            Set<ConditionTag> invalidSpawnConditions = spawnRequirements.stream().filter(x -> !x.check(spawner, stackSettings, block)).collect(Collectors.toSet());
            boolean passedSpawnerChecks = invalidSpawnConditions.isEmpty();

            invalidSpawnConditions.addAll(perSpawnConditions); // Will be removed when they pass

            // Spawn the mobs
            int spawnAmount;
            if (randomizeSpawnAmounts) {
                spawnAmount = this.random.nextInt(spawnerTile.getSpawnCount() - stackedSpawner.getStackSize() + 1) + stackedSpawner.getStackSize();
            } else {
                spawnAmount = spawnerTile.getSpawnCount();
            }

            int spawnRange = spawnerTile.getSpawnRange();
            int successfulSpawns = 0;
            for (int i = 0; i < spawnAmount; i++) {
                int attempts = 0;
                while (attempts < maxFailedSpawnAttempts) {
                    int xOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                    int yOffset = this.random.nextInt(3) - 1;
                    int zOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;

                    Location spawnLocation = block.getLocation().clone().add(xOffset + 0.5, yOffset, zOffset + 0.5);

                    Block target = block.getLocation().clone().add(xOffset, yOffset, zOffset).getBlock();

                    boolean invalid = false;
                    for (ConditionTag conditionTag : perSpawnConditions) {
                        if (!conditionTag.check(spawner, stackSettings, target)) {
                            invalid = true;
                        } else {
                            invalidSpawnConditions.remove(conditionTag);
                        }
                    }

                    if (invalid) {
                        attempts++;
                        continue;
                    }

                    if (!passedSpawnerChecks)
                        break;

                    LivingEntity entity = (LivingEntity) block.getWorld().spawn(spawnLocation, entityType.getEntityClass(), spawnedEntity -> {
                        LivingEntity spawnedLivingEntity = (LivingEntity) spawnedEntity;
                        if (stackSettings.isMobAIDisabled())
                            this.disableAI(spawnedLivingEntity);
                        this.tagSpawnedFromSpawner(spawnedLivingEntity);

                        EntityStackSettings entitySettings = stackSettingManager.getEntityStackSettings(spawnedLivingEntity);
                        if (entitySettings != null)
                            entitySettings.applySpawnerSpawnedProperties(spawnedLivingEntity);
                    });

                    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, stackedSpawner.getSpawner());
                    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                    if (spawnerSpawnEvent.isCancelled())
                        entity.remove();

                    if (entity.isValid()) // Don't spawn particles for auto-stacked entities
                        block.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, spawnLocation.clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);

                    successfulSpawns++;
                    break;
                }
            }

            stackedSpawner.getLastInvalidConditions().clear();
            if (successfulSpawns <= 0) {
                if (invalidSpawnConditions.isEmpty()) {
                    stackedSpawner.getLastInvalidConditions().add(NoneConditionTag.class);
                } else {
                    List<Class<? extends ConditionTag>> invalidSpawnConditionClasses = new ArrayList<>();
                    for (ConditionTag conditionTag : invalidSpawnConditions)
                        invalidSpawnConditionClasses.add(conditionTag.getClass());
                    stackedSpawner.getLastInvalidConditions().addAll(invalidSpawnConditionClasses);
                }

                // Spawn particles indicating the spawn occurred
                block.getWorld().spawnParticle(Particle.SMOKE_NORMAL, block.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
            } else {
                // Spawn particles indicating the spawn did not occur
                block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                PersistentDataUtils.increaseSpawnCount(spawner, successfulSpawns);
            }
        }
    }

    private void tagSpawnedFromSpawner(LivingEntity entity) {
        if (NMSUtil.getVersionNumber() > 13) {
            entity.getPersistentDataContainer().set(new NamespacedKey(this.rosePlugin, METADATA_NAME), PersistentDataType.INTEGER, 1);
        } else {
            entity.setMetadata(METADATA_NAME, new FixedMetadataValue(this.rosePlugin, true));
        }
    }

    /**
     * Checks if an entity was spawned from one of our spawners
     *
     * @param entity The entity to check
     * @return true if the entity was spawned from one of our spawners, otherwise false
     */
    public boolean isSpawnedFromSpawner(LivingEntity entity) {
        if (NMSUtil.getVersionNumber() > 13) {
            return entity.getPersistentDataContainer().has(new NamespacedKey(this.rosePlugin, METADATA_NAME), PersistentDataType.INTEGER);
        } else {
            return entity.hasMetadata(METADATA_NAME);
        }
    }

    /**
     * Disables the AI/knockback of a mob without using {@link LivingEntity#setAI}.
     *
     * @param entity The entity to disable AI for
     */
    public void disableAI(LivingEntity entity) {
        // Remove all applicable AI goals
        PersistentDataUtils.removeEntityAi(entity);

        // Make the entity unable to take knockback
        AttributeInstance knockbackAttribute = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null)
            knockbackAttribute.setBaseValue(Double.MAX_VALUE);
    }

}
