package dev.rosewood.rosestacker.spawning;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.hook.SpawnerFlagPersistenceHook;
import dev.rosewood.rosestacker.hook.WorldGuardHook;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NoneConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NotPlayerPlacedConditionTag;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.util.Vector;

public class MobSpawningMethod implements SpawningMethod {

    private final EntityType entityType;
    private final Random random;

    public MobSpawningMethod(EntityType entityType) {
        this.entityType = entityType;
        this.random = new Random();
    }

    @Override
    public void spawn(StackedSpawner stackedSpawner, boolean onlyCheckConditions) {
        StackedSpawnerTile spawnerTile = stackedSpawner.getSpawnerTile();
        SpawnerStackSettings stackSettings = stackedSpawner.getStackSettings();
        EntityStackSettings entityStackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(this.entityType);

        // Mob spawning logic
        List<ConditionTag> spawnRequirements = new ArrayList<>(stackSettings.getSpawnRequirements());

        // Check general spawner conditions
        List<ConditionTag> perSpawnConditions = spawnRequirements.stream().filter(ConditionTag::isRequiredPerSpawn).toList();
        spawnRequirements.removeAll(perSpawnConditions);

        Set<ConditionTag> invalidSpawnConditions = spawnRequirements.stream().filter(x -> !x.check(stackedSpawner, stackedSpawner.getBlock())).collect(Collectors.toSet());
        if (Setting.SPAWNER_SPAWN_ONLY_PLAYER_PLACED.getBoolean() && !stackedSpawner.isPlacedByPlayer())
            invalidSpawnConditions.add(NotPlayerPlacedConditionTag.INSTANCE);

        boolean passedSpawnerChecks = invalidSpawnConditions.isEmpty();

        invalidSpawnConditions.addAll(perSpawnConditions); // Will be removed when they pass

        // Spawn the mobs
        int spawnAmount;
        if (Setting.SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED.getBoolean()) {
            if (stackSettings.getSpawnCountStackSizeMultiplier() != -1) {
                int spawnerSpawnCount = Math.max(spawnerTile.getSpawnCount(), 0);
                spawnAmount = StackerUtils.randomInRange(stackedSpawner.getStackSize(), spawnerSpawnCount);
            } else {
                spawnAmount = this.random.nextInt(spawnerTile.getSpawnCount()) + 1;
            }
        } else {
            spawnAmount = spawnerTile.getSpawnCount();
        }

        EntityCacheManager entityCacheManager = RoseStacker.getInstance().getManager(EntityCacheManager.class);
        StackManager stackManager = RoseStacker.getInstance().getManager(StackManager.class);

        ThreadUtils.runAsync(() -> {
            // Make sure the chunk is still loaded
            if (!stackedSpawner.getWorld().isChunkLoaded(stackedSpawner.getLocation().getBlockX() >> 4, stackedSpawner.getLocation().getBlockZ() >> 4))
                return;

            Set<Location> spawnLocations = new HashSet<>();
            Set<Location> invalidLocations = new HashSet<>();
            int spawnRange = spawnerTile.getSpawnRange();
            int attempts = 0;
            int maxFailedSpawnAttempts = Setting.SPAWNER_MAX_FAILED_SPAWN_ATTEMPTS.getInt() * spawnRange * spawnRange;
            int desiredLocations = Math.max(2, stackSettings.getSpawnCountStackSizeMultiplier());
            if (!this.useNearbyEntitiesForStacking(stackManager, entityStackSettings))
                desiredLocations *= 4;

            while (attempts <= maxFailedSpawnAttempts) {
                int xOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                int yOffset = !Setting.SPAWNER_USE_VERTICAL_SPAWN_RANGE.getBoolean() ? this.random.nextInt(3) - 1 : this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                int zOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;

                Location spawnLocation = stackedSpawner.getLocation().clone().add(xOffset + 0.5, yOffset, zOffset + 0.5);
                if (invalidLocations.contains(spawnLocation)) {
                    // Decrease max failed spawn attempts if the location is invalid to avoid spinning forever
                    maxFailedSpawnAttempts--;
                    continue;
                }

                Block target = stackedSpawner.getLocation().clone().add(xOffset, yOffset, zOffset).getBlock();

                boolean invalid = false;
                for (ConditionTag conditionTag : perSpawnConditions) {
                    if (!conditionTag.check(stackedSpawner, target)) {
                        invalid = true;
                    } else {
                        invalidSpawnConditions.remove(conditionTag);
                    }
                }

                if (invalid) {
                    invalidLocations.add(spawnLocation);
                    attempts++;
                    continue;
                }

                if (!passedSpawnerChecks)
                    break;

                spawnLocations.add(spawnLocation);
                if (spawnLocations.size() >= desiredLocations)
                    break;
            }

            EntityType entityType = stackedSpawner.getSpawnerTile().getSpawnerType().getOrThrow();
            Predicate<Entity> predicate = entity -> entity.getType() == entityType;
            Collection<Entity> nearbyEntities = entityCacheManager.getNearbyEntities(stackedSpawner.getLocation(), stackSettings.getSpawnRange(), predicate);
            List<StackedEntity> nearbyStackedEntities = new ArrayList<>();
            for (Entity entity : nearbyEntities) {
                StackedEntity stackedEntity = stackManager.getStackedEntity((LivingEntity) entity);
                if (stackedEntity != null)
                    nearbyStackedEntities.add(stackedEntity);
            }

            int successfulSpawns;
            if (!onlyCheckConditions) {
                successfulSpawns = this.spawnEntitiesIntoNearbyStacks(stackedSpawner, spawnAmount, spawnLocations, nearbyStackedEntities, stackManager, entityStackSettings);
            } else {
                successfulSpawns = spawnAmount > 0 && !spawnLocations.isEmpty() ? 1 : 0;
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

                if (!onlyCheckConditions) {
                    // Spawn particles indicating the spawn did not occur
                    stackedSpawner.getWorld().spawnParticle(Particle.SMOKE_NORMAL, stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                }
            } else if (!onlyCheckConditions) {
                // Spawn particles indicating the spawn occurred
                stackedSpawner.getWorld().spawnParticle(Particle.FLAME, stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                ThreadUtils.runSync(() -> {
                    if (stackedSpawner.getBlock().getType() == Material.SPAWNER)
                        PersistentDataUtils.increaseSpawnCount(spawnerTile, successfulSpawns);
                });
            }
        });
    }

    private int spawnEntitiesIntoNearbyStacks(StackedSpawner stackedSpawner, int spawnAmount, Set<Location> locations, List<StackedEntity> nearbyEntities, StackManager stackManager, EntityStackSettings entityStackSettings) {
        List<StackedEntity> stackedEntities = new ArrayList<>(nearbyEntities);
        List<Location> possibleLocations = new ArrayList<>(locations);

        if (this.entityType.getEntityClass() == null)
            return 0;

        int successfulSpawns = 0;
        if (this.useNearbyEntitiesForStacking(stackManager, entityStackSettings)) {
            List<StackedEntity> newStacks = new ArrayList<>();
            NMSHandler nmsHandler = NMSAdapter.getHandler();

            List<StackedEntity> updatedStacks = new ArrayList<>();

            Location previousLocation = null;
            for (int i = spawnAmount; i > 0; i--) {
                Location location = possibleLocations.isEmpty() ? previousLocation : possibleLocations.get(this.random.nextInt(possibleLocations.size()));
                if (location == null)
                    break;

                switch (stackManager.getEntityDataStorageType()) {
                    case NBT -> {
                        StackedEntity newStack = this.createNewEntity(nmsHandler, location, stackedSpawner, entityStackSettings);
                        Optional<StackedEntity> matchingEntity = stackedEntities.stream().filter(x ->
                                WorldGuardHook.testLocation(x.getLocation()) && entityStackSettings.testCanStackWith(x, newStack, false, true)).findAny();
                        if (matchingEntity.isPresent()) {
                            matchingEntity.get().increaseStackSize(newStack.getEntity(), false);
                            updatedStacks.add(matchingEntity.get());
                        } else {
                            if (possibleLocations.isEmpty())
                                break;

                            stackedEntities.add(newStack);
                            newStacks.add(newStack);
                            possibleLocations.remove(location);
                        }

                        previousLocation = location;
                        successfulSpawns++;
                    }

                    case SIMPLE -> {
                        Optional<StackedEntity> matchingEntity = stackedEntities.stream().filter(x ->
                                WorldGuardHook.testLocation(x.getLocation()) && entityStackSettings.testCanStackWith(x, x, false, true)).findAny();
                        if (matchingEntity.isPresent()) {
                            // Increase stack size by as much as we can
                            int amountToIncrease = Math.min(i, entityStackSettings.getMaxStackSize() - matchingEntity.get().getStackSize());
                            matchingEntity.get().increaseStackSize(amountToIncrease, false);
                            updatedStacks.add(matchingEntity.get());
                            i -= amountToIncrease;
                            successfulSpawns += amountToIncrease;
                        } else {
                            if (possibleLocations.isEmpty())
                                break;

                            StackedEntity newStack = this.createNewEntity(nmsHandler, location, stackedSpawner, entityStackSettings);
                            stackedEntities.add(newStack);
                            newStacks.add(newStack);
                            possibleLocations.remove(location);
                            successfulSpawns++;
                        }

                        previousLocation = location;
                    }
                }
            }

            updatedStacks.forEach(StackedEntity::updateDisplay);

            ThreadUtils.runSync(() -> {
                stackManager.setEntityStackingTemporarilyDisabled(true);
                for (StackedEntity stackedEntity : newStacks) {
                    LivingEntity entity = stackedEntity.getEntity();

                    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, stackedSpawner.getSpawner());
                    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                    if (spawnerSpawnEvent.isCancelled())
                        continue;

                    nmsHandler.spawnExistingEntity(stackedEntity.getEntity(), CreatureSpawnEvent.SpawnReason.SPAWNER, Setting.SPAWNER_BYPASS_REGION_SPAWNING_RULES.getBoolean());
                    entity.setVelocity(Vector.getRandom().multiply(0.01));
                    stackManager.addEntityStack(stackedEntity);
                }
                stackManager.setEntityStackingTemporarilyDisabled(false);

                // Spawn particles for new entities and update nametags
                for (StackedEntity entity : newStacks) {
                    entity.updateDisplay();
                    World world = entity.getLocation().getWorld();
                    if (world != null)
                        world.spawnParticle(Particle.EXPLOSION_NORMAL, entity.getLocation().clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);
                }
            });
        } else {
            successfulSpawns = Math.min(spawnAmount, possibleLocations.size());

            ThreadUtils.runSync(() -> {
                NMSHandler nmsHandler = NMSAdapter.getHandler();
                for (int i = 0; i < spawnAmount; i++) {
                    if (possibleLocations.isEmpty())
                        break;

                    Location location = possibleLocations.get(this.random.nextInt(possibleLocations.size()));
                    World world = location.getWorld();
                    if (world == null)
                        continue;

                    LivingEntity entity = nmsHandler.spawnEntityWithReason(this.entityType, location, CreatureSpawnEvent.SpawnReason.SPAWNER, Setting.SPAWNER_BYPASS_REGION_SPAWNING_RULES.getBoolean());
                    entityStackSettings.applySpawnerSpawnedProperties(entity);

                    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, stackedSpawner.getSpawner());
                    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                    if (spawnerSpawnEvent.isCancelled()) {
                        entity.remove();
                        continue;
                    }

                    // Nudge the entities a little so they unstack easier
                    entity.setVelocity(Vector.getRandom().multiply(0.01));

                    // Spawn Particles
                    if (entity.isValid())
                        entity.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, entity.getLocation().clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);
                }
            });
        }

        return successfulSpawns;
    }

    private boolean useNearbyEntitiesForStacking(StackManager stackManager, EntityStackSettings entityStackSettings) {
        return stackManager.isEntityStackingEnabled() && entityStackSettings.isStackingEnabled() && Setting.SPAWNER_SPAWN_INTO_NEARBY_STACKS.getBoolean();
    }

    private StackedEntity createNewEntity(NMSHandler nmsHandler, Location location, StackedSpawner stackedSpawner, EntityStackSettings entityStackSettings) {
        LivingEntity entity = nmsHandler.createNewEntityUnspawned(this.entityType, location, CreatureSpawnEvent.SpawnReason.SPAWNER);
        SpawnerFlagPersistenceHook.flagSpawnerSpawned(entity);

        if ((stackedSpawner.getStackSettings().isMobAIDisabled() && (!Setting.SPAWNER_DISABLE_MOB_AI_ONLY_PLAYER_PLACED.getBoolean() || stackedSpawner.isPlacedByPlayer())) || Setting.ENTITY_DISABLE_ALL_MOB_AI.getBoolean())
            PersistentDataUtils.removeEntityAi(entity);

        entityStackSettings.applySpawnerSpawnedProperties(entity);

        return new StackedEntity(entity);
    }

}
