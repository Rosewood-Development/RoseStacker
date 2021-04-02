package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.object.SpawnerTileWrapper;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.NoneConditionTag;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class SpawnerSpawnManager extends Manager implements Runnable {

    /**
     * Metadata name used to keep track of if an entity is spawned from a spawner
     */
    private static final String METADATA_NAME = "spawner_spawned";

    /**
     * At what point should we override the normal spawner spawning?
     */
    public static final int DELAY_THRESHOLD = 3;

    private final StackManager stackManager;
    private final Random random;
    private BukkitTask task;

    public SpawnerSpawnManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.stackManager = this.rosePlugin.getManager(StackManager.class);
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
        EntityCacheManager entityCacheManager = this.rosePlugin.getManager(EntityCacheManager.class);

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
                int spawnerSpawnCount = Math.max(stackedSpawner.getStackSize() * stackSettings.getSpawnCountStackSizeMultiplier(), 0);
                spawnAmount = this.random.nextInt(spawnerSpawnCount - stackedSpawner.getStackSize() + 1) + stackedSpawner.getStackSize();
            } else {
                spawnAmount = spawnerTile.getSpawnCount();
            }

            Set<Location> spawnLocations = new HashSet<>();
            int spawnRange = spawnerTile.getSpawnRange();
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

                    spawnLocations.add(spawnLocation);
                    break;
                }
            }

            Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
                Predicate<Entity> predicate = entity -> entity.getType() == entityType;
                Collection<Entity> nearbyEntities = entityCacheManager.getNearbyEntities(spawner.getLocation(), stackSettings.getSpawnRange(), predicate);
                List<StackedEntity> nearbyStackedEntities = new ArrayList<>();
                for (Entity entity : nearbyEntities) {
                    StackedEntity stackedEntity = this.stackManager.getStackedEntity((LivingEntity) entity);
                    if (stackedEntity != null)
                        nearbyStackedEntities.add(stackedEntity);
                }

                int successfulSpawns = this.spawnEntitiesIntoNearbyStacks(stackedSpawner, entityType, spawnAmount, spawnLocations, nearbyStackedEntities);

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

                    // Spawn particles indicating the spawn did not occur
                    block.getWorld().spawnParticle(Particle.SMOKE_NORMAL, block.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                } else {
                    // Spawn particles indicating the spawn occurred
                    block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                    Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                        stackedSpawner.updateSpawnerState();
                        PersistentDataUtils.increaseSpawnCount(stackedSpawner.getSpawner(), successfulSpawns);
                        stackedSpawner.updateSpawnerState();
                    });
                }
            });
        }
    }

    private int spawnEntitiesIntoNearbyStacks(StackedSpawner spawner, EntityType entityType, int spawnAmount, Set<Location> locations, List<StackedEntity> nearbyEntities) {
        EntityStackSettings entityStackSettings = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType);
        List<StackedEntity> stackedEntities = new ArrayList<>(nearbyEntities);
        List<Location> possibleLocations = new ArrayList<>(locations);

        int successfulSpawns = 0;
        if (this.stackManager.isEntityStackingEnabled() && entityStackSettings.isStackingEnabled()) {
            List<StackedEntity> newStacks = new ArrayList<>();
            NMSHandler nmsHandler = NMSAdapter.getHandler();

            for (int i = 0; i < spawnAmount; i++) {
                if (possibleLocations.isEmpty())
                    break;

                Location location = possibleLocations.get(this.random.nextInt(possibleLocations.size()));
                LivingEntity entity;
                try {
                    entity = nmsHandler.createEntityUnspawned(entityType, location.clone().subtract(0, 300, 0));
                } catch (IllegalStateException e) {
                    continue; // This is here due to jockeys trying to add entities to the world async, no bueno
                }

                if (spawner.getStackSettings().isMobAIDisabled())
                    PersistentDataUtils.removeEntityAi(entity);
                this.tagSpawnedFromSpawner(entity);

                entityStackSettings.applySpawnerSpawnedProperties(entity);

                StackedEntity newStack = new StackedEntity(entity);
                Optional<StackedEntity> matchingEntity = stackedEntities.stream().filter(x ->
                        entityStackSettings.testCanStackWith(x, newStack, false)).findFirst();
                if (matchingEntity.isPresent()) {
                    matchingEntity.get().increaseStackSize(entity);
                } else {
                    stackedEntities.add(newStack);
                    newStacks.add(newStack);
                    possibleLocations.remove(location);
                }

                successfulSpawns++;
            }

            Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                this.stackManager.setEntityStackingTemporarilyDisabled(true);
                for (StackedEntity stackedEntity : newStacks) {
                    LivingEntity entity = stackedEntity.getEntity();

                    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, spawner.getSpawner());
                    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                    if (spawnerSpawnEvent.isCancelled())
                        continue;

                    entity.teleport(entity.getLocation().clone().add(0, 300, 0));
                    nmsHandler.spawnExistingEntity(stackedEntity.getEntity(), CreatureSpawnEvent.SpawnReason.SPAWNER);
                    entity.setVelocity(Vector.getRandom().multiply(0.01));
                    this.stackManager.addEntityStack(stackedEntity);
                }
                this.stackManager.setEntityStackingTemporarilyDisabled(false);

                // Spawn particles for new entities
                for (StackedEntity entity : newStacks) {
                    World world = entity.getLocation().getWorld();
                    if (world != null)
                        world.spawnParticle(Particle.EXPLOSION_NORMAL, entity.getLocation().clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);
                }
            });
        } else {
            successfulSpawns = Math.min(spawnAmount, possibleLocations.size());

            Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                for (int i = 0; i < spawnAmount; i++) {
                    if (possibleLocations.isEmpty())
                        break;

                    Location location = possibleLocations.remove(this.random.nextInt(possibleLocations.size()));
                    World world = location.getWorld();
                    if (world == null)
                        continue;

                    LivingEntity entity = (LivingEntity) world.spawnEntity(location, entityType);

                    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, spawner.getSpawner());
                    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                    if (spawnerSpawnEvent.isCancelled()) {
                        entity.remove();
                        continue;
                    }

                    if (spawner.getStackSettings().isMobAIDisabled())
                        PersistentDataUtils.removeEntityAi(entity);
                    this.tagSpawnedFromSpawner(entity);

                    entityStackSettings.applySpawnerSpawnedProperties(entity);

                    // Spawn Particles
                    entity.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, entity.getLocation().clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);
                }
            });
        }

        return successfulSpawns;
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

}
