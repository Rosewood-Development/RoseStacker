package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.NoneConditionTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        boolean randomizeSpawnAmounts = Setting.SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED.getBoolean();
        int maxFailedSpawnAttempts = Setting.SPAWNER_MAX_FAILED_SPAWN_ATTEMPTS.getInt();
        boolean redstoneSpawners = Setting.SPAWNER_DEACTIVATE_WHEN_POWERED.getBoolean();

        Map<Block, StackedSpawner> stackedSpawners = stackManager.getStackedSpawners();
        for (Block block : stackedSpawners.keySet()) {
            if (block.getType() != Material.SPAWNER)
                continue;

            StackedSpawner stackedSpawner = stackedSpawners.get(block);
            SpawnerStackSettings stackSettings = stackedSpawner.getStackSettings();
            CreatureSpawner spawner = (CreatureSpawner) block.getState(); // Need to refetch the state so the delay is the latest
            if (redstoneSpawners) {
                boolean isPowered = block.isBlockPowered();
                boolean wasPowered = stackedSpawner.isPowered();
                boolean changed = false;
                if (isPowered && !wasPowered) {
                    // Prevent the spawner from spinning and counting down the delay (in most cases)
                    spawner.setRequiredPlayerRange(1);
                    changed = true;
                } else if (!isPowered && wasPowered) {
                    spawner.setRequiredPlayerRange(stackSettings.getPlayerActivationRange());
                    changed = true;
                }

                int delay = spawner.getDelay();
                if (isPowered) {
                    int lastDelay = stackedSpawner.getLastDelay();

                    // If the spawner is still spinning, prevent it from counting down
                    if (lastDelay != delay) {
                        spawner.setDelay(lastDelay);
                        changed = true;
                    }
                }

                if (changed) {
                    spawner.update(false, false);
                    stackedSpawner.setPowered(isPowered);
                }
            }

            stackedSpawner.setLastDelay(spawner.getDelay());
            if (stackedSpawner.getLastDelay() >= DELAY_THRESHOLD)
                continue;

            EntityType entityType = spawner.getSpawnedType();
            if (entityType.getEntityClass() == null || !LivingEntity.class.isAssignableFrom(entityType.getEntityClass()))
                return;

            // Reset the spawn delay
            int newDelay = this.random.nextInt(spawner.getMaxSpawnDelay() - spawner.getMinSpawnDelay() + 1) + spawner.getMinSpawnDelay();
            stackedSpawner.setLastDelay(newDelay);
            spawner.setDelay(newDelay);
            spawner.update(false, false);

            // Spawn particles indicating the spawn occurred
            block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);

            List<ConditionTag> spawnRequirements = new ArrayList<>(stackSettings.getSpawnRequirements());

            // Check general spawner conditions
            List<ConditionTag> perSpawnConditions = spawnRequirements.stream().filter(ConditionTag::isRequiredPerSpawn).collect(Collectors.toList());
            spawnRequirements.removeAll(perSpawnConditions);

            Set<ConditionTag> invalidSpawnConditions = spawnRequirements.stream().filter(x -> !x.check(spawner, block)).collect(Collectors.toSet());
            boolean passedSpawnerChecks = invalidSpawnConditions.isEmpty();

            invalidSpawnConditions.addAll(perSpawnConditions); // Will be removed when they pass

            // Spawn the mobs
            int spawnAmount;
            if (randomizeSpawnAmounts) {
                spawnAmount = this.random.nextInt(spawner.getSpawnCount() - stackedSpawner.getStackSize() + 1) + stackedSpawner.getStackSize();
            } else {
                spawnAmount = spawner.getSpawnCount();
            }

            int spawnRange = spawner.getSpawnRange();
            boolean successfulSpawn = false;
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
                        if (!conditionTag.check(spawner, target)) {
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
                    });

                    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, spawner);
                    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                    if (spawnerSpawnEvent.isCancelled())
                        entity.remove();

                    if (entity.isValid()) // Don't spawn particles for auto-stacked entities
                        block.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, spawnLocation.clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);

                    successfulSpawn = true;
                    break;
                }
            }

            stackedSpawner.getLastInvalidConditions().clear();
            if (!successfulSpawn) {
                if (invalidSpawnConditions.isEmpty()) {
                    stackedSpawner.getLastInvalidConditions().add(NoneConditionTag.class);
                } else {
                    List<Class<? extends ConditionTag>> invalidSpawnConditionClasses = new ArrayList<>();
                    for (ConditionTag conditionTag : invalidSpawnConditions)
                        invalidSpawnConditionClasses.add(conditionTag.getClass());
                    stackedSpawner.getLastInvalidConditions().addAll(invalidSpawnConditionClasses);
                }
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
     * Disables the movement/knockback AI of a mob without using {@link LivingEntity#setAI}.
     *
     * @param entity The entity to disable AI for
     */
    public void disableAI(LivingEntity entity) {
        // Make the entity unable to move
        AttributeInstance movementAttribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementAttribute != null)
            movementAttribute.setBaseValue(0);

        // Make the entity unable to take knockback
        AttributeInstance knockbackAttribute = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null)
            knockbackAttribute.setBaseValue(Double.MAX_VALUE);

        // Supposed to stop jumping, but only seems to work on players
        //entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, true, false));
    }

}
