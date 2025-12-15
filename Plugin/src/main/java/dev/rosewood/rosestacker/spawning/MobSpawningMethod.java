package dev.rosewood.rosestacker.spawning;

import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.PreStackedSpawnerSpawnEvent;
import dev.rosewood.rosestacker.hook.WorldGuardHook;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.MaxNearbyEntityConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NoneConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NotPlayerPlacedConditionTag;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import dev.rosewood.rosestacker.utils.VersionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
        if (SettingKey.SPAWNER_SPAWN_ONLY_PLAYER_PLACED.get() && !stackedSpawner.isPlacedByPlayer())
            invalidSpawnConditions.add(NotPlayerPlacedConditionTag.INSTANCE);

        boolean passedSpawnerChecks = invalidSpawnConditions.isEmpty();

        invalidSpawnConditions.addAll(perSpawnConditions); // Will be removed when they pass

        // Spawn the mobs
        int spawnAmount;
        if (SettingKey.SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED.get()) {
            if (stackSettings.getSpawnCountStackSizeMultiplier() != -1) {
                int spawnerSpawnCount = Math.max(spawnerTile.getSpawnCount(), 0);
                spawnAmount = StackerUtils.randomInRange(stackedSpawner.getStackSize(), spawnerSpawnCount);
            } else {
                spawnAmount = this.random.nextInt(spawnerTile.getSpawnCount()) + 1;
            }
        } else {
            spawnAmount = spawnerTile.getSpawnCount();
        }

        StackManager stackManager = RoseStacker.getInstance().getManager(StackManager.class);

        Runnable spawnTask = () -> {
            // Make sure the chunk is still loaded
            if (!stackedSpawner.getWorld().isChunkLoaded(stackedSpawner.getLocation().getBlockX() >> 4, stackedSpawner.getLocation().getBlockZ() >> 4))
                return;

            Set<Location> spawnLocations = new HashSet<>();
            Set<Location> invalidLocations = new HashSet<>();

            int spawnRange = spawnerTile.getSpawnRange();
            int attempts = 0;
            int maxFailedSpawnAttempts = SettingKey.SPAWNER_MAX_FAILED_SPAWN_ATTEMPTS.get() * spawnRange * spawnRange;
            int desiredLocations = Math.max(2, stackSettings.getSpawnCountStackSizeMultiplier());
            boolean useNearbyEntitiesForStacking = stackManager.isEntityStackingEnabled() && entityStackSettings.isStackingEnabled() && SettingKey.SPAWNER_SPAWN_INTO_NEARBY_STACKS.get();
            if (!useNearbyEntitiesForStacking)
                desiredLocations *= 4;

            while (attempts <= maxFailedSpawnAttempts) {
                int xOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                int yOffset = !SettingKey.SPAWNER_USE_VERTICAL_SPAWN_RANGE.get() ? this.random.nextInt(3) - 1 : this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
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

            int successfulSpawns;
            if (!onlyCheckConditions) {
                if (useNearbyEntitiesForStacking) {
                    successfulSpawns = this.spawnEntitiesIntoNearbyStacks(stackedSpawner, spawnAmount, spawnLocations, stackManager, stackSettings, entityStackSettings, invalidSpawnConditions);
                    if (successfulSpawns > 0)
                        invalidSpawnConditions.removeIf(x -> x instanceof MaxNearbyEntityConditionTag);
                } else {
                    successfulSpawns = this.spawnEntitiesIndividually(stackedSpawner, spawnAmount, spawnLocations, entityStackSettings, stackManager);
                }
            } else {
                successfulSpawns = 0;
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
                    stackedSpawner.getWorld().spawnParticle(VersionUtils.SMOKE, stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                }
            } else {
                // Spawn particles indicating the spawn occurred
                stackedSpawner.getWorld().spawnParticle(Particle.FLAME, stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                ThreadUtils.runSync(() -> {
                    if (stackedSpawner.getBlock().getType() == Material.SPAWNER)
                        PersistentDataUtils.increaseSpawnCount(spawnerTile, successfulSpawns);
                });
            }
        };

        if (SettingKey.SPAWNER_SPAWN_ASYNC.get()) {
            ThreadUtils.runAsync(spawnTask);
        } else {
            spawnTask.run();
        }
    }

    private int spawnEntitiesIndividually(StackedSpawner stackedSpawner, int spawnAmount, Set<Location> locations, EntityStackSettings entityStackSettings, StackManager stackManager) {
        if (this.entityType.getEntityClass() == null || locations.isEmpty())
            return 0;

        PreStackedSpawnerSpawnEvent event = new PreStackedSpawnerSpawnEvent(stackedSpawner, spawnAmount);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return 0;

        spawnAmount = event.getSpawnAmount();

        boolean bypassRegions = SettingKey.SPAWNER_BYPASS_REGION_SPAWNING_RULES.get();

        List<Location> possibleLocations = new ArrayList<>(locations);
        int finalSpawnAmount = spawnAmount;
        ThreadUtils.runSync(() -> { // No poof particles to show where the mobs spawn with this setting, they immediately try stacking and are entirely unpredictable
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            for (int i = 0; i < finalSpawnAmount; i++) {
                if (locations.isEmpty())
                    break;

                Location location = possibleLocations.get(this.random.nextInt(possibleLocations.size()));
                if (NMSUtil.isPaper()) {
                    var result = PreCreatureSpawnEventHelper.call(location, this.entityType, CreatureSpawnEvent.SpawnReason.SPAWNER);
                    if (result.abort())
                        return;
                    if (result.cancel())
                        continue;
                }

                LivingEntity entity = nmsHandler.spawnEntityWithReason(this.entityType, location, CreatureSpawnEvent.SpawnReason.SPAWNER, bypassRegions);

                SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, stackedSpawner.getSpawner());
                Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                if (spawnerSpawnEvent.isCancelled()) {
                    entity.remove();
                    continue;
                }

                if (bypassRegions) {
                    if (!stackManager.isAreaDisabled(location)) {
                        if ((stackedSpawner.getStackSettings().isMobAIDisabled() && (!SettingKey.SPAWNER_DISABLE_MOB_AI_ONLY_PLAYER_PLACED.get() || stackedSpawner.isPlacedByPlayer())) || entityStackSettings.isMobAIDisabled())
                            PersistentDataUtils.removeEntityAi(entity);

                        entityStackSettings.applySpawnerSpawnedProperties(entity);
                    }
                }

                // Nudge the entities a little so they unstack easier
                entity.setVelocity(Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).multiply(0.01));
            }
        });

        return spawnAmount;
    }

    private int spawnEntitiesIntoNearbyStacks(StackedSpawner stackedSpawner, int spawnAmount, Set<Location> locations, StackManager stackManager, SpawnerStackSettings spawnerStackSettings, EntityStackSettings entityStackSettings, Set<ConditionTag> invalidSpawnConditions) {
        if (!invalidSpawnConditions.isEmpty() && !invalidSpawnConditions.stream().allMatch(x -> x instanceof MaxNearbyEntityConditionTag))
            return 0;

        boolean canSpawnNewEntities = invalidSpawnConditions.isEmpty();
        EntityType entityType = stackedSpawner.getSpawnerTile().getSpawnerType().getOrThrow();
        Predicate<Entity> predicate = entity -> entity.getType() == entityType;
        EntityCacheManager entityCacheManager = RoseStacker.getInstance().getManager(EntityCacheManager.class);
        Collection<Entity> nearbyEntities = entityCacheManager.getNearbyEntities(stackedSpawner.getLocation(), spawnerStackSettings.getSpawnRange(), predicate);
        List<StackedEntity> nearbyStackedEntities = new ArrayList<>();
        for (Entity entity : nearbyEntities) {
            StackedEntity stackedEntity = stackManager.getStackedEntity((LivingEntity) entity);
            if (stackedEntity != null && stackedEntity.getStackSize() < entityStackSettings.getMaxStackSize())
                nearbyStackedEntities.add(stackedEntity);
        }

        List<Location> possibleLocations = new ArrayList<>(locations);

        if (this.entityType.getEntityClass() == null || (possibleLocations.isEmpty() && nearbyStackedEntities.isEmpty()))
            return 0;

        PreStackedSpawnerSpawnEvent event = new PreStackedSpawnerSpawnEvent(stackedSpawner, spawnAmount);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return 0;

        spawnAmount = event.getSpawnAmount();

        int successfulSpawns = 0;
        List<StackedEntity> newStacks = new ArrayList<>();
        NMSHandler nmsHandler = NMSAdapter.getHandler();

        List<StackedEntity> updatedStacks = new ArrayList<>();

        for (int i = spawnAmount; i > 0; i--) {
            Location location = possibleLocations.isEmpty() ? stackedSpawner.getLocation() : possibleLocations.get(this.random.nextInt(possibleLocations.size()));
            switch (stackManager.getEntityDataStorageType(this.entityType)) {
                case NBT -> {
                    StackedEntity newStack = this.createNewEntity(nmsHandler, location, stackedSpawner, stackManager, entityStackSettings);
                    Optional<StackedEntity> matchingEntity = nearbyStackedEntities.stream().filter(x ->
                            WorldGuardHook.testLocation(x.getLocation()) && entityStackSettings.testCanStackWith(x, newStack, false, true)).findAny();
                    if (matchingEntity.isPresent()) {
                        matchingEntity.get().increaseStackSize(newStack.getEntity(), false);
                        updatedStacks.add(matchingEntity.get());
                    } else if (canSpawnNewEntities) {
                        if (possibleLocations.isEmpty())
                            break;

                        nearbyStackedEntities.add(newStack);
                        newStacks.add(newStack);
                    }

                    successfulSpawns++;
                }

                case SIMPLE -> {
                    Optional<StackedEntity> matchingEntity = nearbyStackedEntities.stream().filter(x ->
                            WorldGuardHook.testLocation(x.getLocation()) && entityStackSettings.testCanStackWith(x, x, false, true)).findAny();
                    if (matchingEntity.isPresent()) {
                        // Increase stack size by as much as we can
                        int amountToIncrease = Math.min(i, entityStackSettings.getMaxStackSize() - matchingEntity.get().getStackSize());
                        matchingEntity.get().increaseStackSize(amountToIncrease, false);
                        updatedStacks.add(matchingEntity.get());
                        i -= amountToIncrease;
                        successfulSpawns += amountToIncrease;
                    } else if (canSpawnNewEntities) {
                        StackedEntity newStack = this.createNewEntity(nmsHandler, location, stackedSpawner, stackManager, entityStackSettings);
                        nearbyStackedEntities.add(newStack);
                        newStacks.add(newStack);
                        successfulSpawns++;
                    }
                }
            }
        }

        updatedStacks.forEach(StackedEntity::updateDisplay);

        ThreadUtils.runSync(() -> {
            stackManager.setEntityStackingTemporarilyDisabled(true);
            Iterator<StackedEntity> iterator = newStacks.iterator();
            while (iterator.hasNext()) {
                StackedEntity stackedEntity = iterator.next();
                LivingEntity entity = stackedEntity.getEntity();

                if (NMSUtil.isPaper()) {
                    var result = PreCreatureSpawnEventHelper.call(entity.getLocation(), this.entityType, CreatureSpawnEvent.SpawnReason.SPAWNER);
                    if (result.abort()) {
                        newStacks.clear();
                        break;
                    }
                    if (result.cancel()) {
                        iterator.remove();
                        continue;
                    }
                }

                SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, stackedSpawner.getSpawner());
                Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                if (spawnerSpawnEvent.isCancelled())
                    continue;

                nmsHandler.spawnExistingEntity(entity, CreatureSpawnEvent.SpawnReason.SPAWNER, SettingKey.SPAWNER_BYPASS_REGION_SPAWNING_RULES.get());
                entity.setVelocity(Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).multiply(0.01));
                stackManager.addEntityStack(stackedEntity);
            }
            stackManager.setEntityStackingTemporarilyDisabled(false);

            // Spawn particles for new entities and update nametags
            for (StackedEntity entity : newStacks) {
                entity.updateDisplay();
                World world = entity.getLocation().getWorld();
                if (world != null)
                    world.spawnParticle(VersionUtils.POOF, entity.getLocation().clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);
            }
        });

        return successfulSpawns;
    }

    private StackedEntity createNewEntity(NMSHandler nmsHandler, Location location, StackedSpawner stackedSpawner, StackManager stackManager, EntityStackSettings entityStackSettings) {
        LivingEntity entity = nmsHandler.createNewEntityUnspawned(this.entityType, location, CreatureSpawnEvent.SpawnReason.SPAWNER);

        if (!stackManager.isAreaDisabled(location)) {
            if ((stackedSpawner.getStackSettings().isMobAIDisabled() && (!SettingKey.SPAWNER_DISABLE_MOB_AI_ONLY_PLAYER_PLACED.get() || stackedSpawner.isPlacedByPlayer())) || entityStackSettings.isMobAIDisabled())
                PersistentDataUtils.removeEntityAi(entity);

            entityStackSettings.applySpawnerSpawnedProperties(entity);
        }

        return new StackedEntity(entity);
    }

}
