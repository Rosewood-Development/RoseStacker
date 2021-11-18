package dev.rosewood.rosestacker.spawner.spawning;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.hook.SpawnerFlagPersistenceHook;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.object.StackedSpawnerTile;
import dev.rosewood.rosestacker.spawner.conditions.ConditionTag;
import dev.rosewood.rosestacker.spawner.conditions.tags.NoneConditionTag;
import dev.rosewood.rosestacker.spawner.conditions.tags.NotPlayerPlacedConditionTag;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
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
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Ageable;
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
    public void spawn(StackedSpawner stackedSpawner, StackedSpawnerTile spawnerTile) {
        SpawnerStackSettings stackSettings = stackedSpawner.getStackSettings();

        // Mob spawning logic
        List<ConditionTag> spawnRequirements = new ArrayList<>(stackSettings.getSpawnRequirements());

        // Check general spawner conditions
        List<ConditionTag> perSpawnConditions = spawnRequirements.stream().filter(ConditionTag::isRequiredPerSpawn).collect(Collectors.toList());
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

        Bukkit.getScheduler().runTaskAsynchronously(RoseStacker.getInstance(), () -> {
            Set<Location> spawnLocations = new HashSet<>();
            int spawnRange = spawnerTile.getSpawnRange();
            for (int i = 0; i < spawnAmount; i++) {
                int attempts = 0;
                while (attempts < Setting.SPAWNER_MAX_FAILED_SPAWN_ATTEMPTS.getInt()) {
                    int xOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                    int yOffset = !Setting.SPAWNER_USE_VERTICAL_SPAWN_RANGE.getBoolean() ? this.random.nextInt(3) - 1 : this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                    int zOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;

                    Location spawnLocation = stackedSpawner.getLocation().clone().add(xOffset + 0.5, yOffset, zOffset + 0.5);
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
                        attempts++;
                        continue;
                    }

                    if (!passedSpawnerChecks)
                        break;

                    spawnLocations.add(spawnLocation);
                    break;
                }
            }

            EntityType entityType = stackedSpawner.getSpawnerTile().getSpawnedType();
            Predicate<Entity> predicate = entity -> entity.getType() == entityType;
            Collection<Entity> nearbyEntities = entityCacheManager.getNearbyEntities(stackedSpawner.getLocation(), stackSettings.getSpawnRange(), predicate);
            List<StackedEntity> nearbyStackedEntities = new ArrayList<>();
            for (Entity entity : nearbyEntities) {
                StackedEntity stackedEntity = stackManager.getStackedEntity((LivingEntity) entity);
                if (stackedEntity != null)
                    nearbyStackedEntities.add(stackedEntity);
            }

            int successfulSpawns = this.spawnEntitiesIntoNearbyStacks(stackedSpawner, spawnAmount, spawnLocations, nearbyStackedEntities, stackManager);

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
                stackedSpawner.getWorld().spawnParticle(Particle.SMOKE_NORMAL, stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
            } else {
                // Spawn particles indicating the spawn occurred
                stackedSpawner.getWorld().spawnParticle(Particle.FLAME, stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                Bukkit.getScheduler().runTask(RoseStacker.getInstance(), () -> {
                    if (stackedSpawner.getBlock().getType() == Material.SPAWNER)
                        PersistentDataUtils.increaseSpawnCount(spawnerTile, successfulSpawns);
                });
            }
        });
    }

    private int spawnEntitiesIntoNearbyStacks(StackedSpawner stackedSpawner, int spawnAmount, Set<Location> locations, List<StackedEntity> nearbyEntities, StackManager stackManager) {
        EntityStackSettings entityStackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(this.entityType);
        List<StackedEntity> stackedEntities = new ArrayList<>(nearbyEntities);
        List<Location> possibleLocations = new ArrayList<>(locations);

        if (this.entityType.getEntityClass() == null)
            return 0;

        boolean ageable = Ageable.class.isAssignableFrom(this.entityType.getEntityClass());

        int successfulSpawns = 0;
        if (stackManager.isEntityStackingEnabled() && entityStackSettings.isStackingEnabled() && Setting.SPAWNER_SPAWN_INTO_NEARBY_STACKS.getBoolean()) {
            List<StackedEntity> newStacks = new ArrayList<>();
            NMSHandler nmsHandler = NMSAdapter.getHandler();

            for (int i = 0; i < spawnAmount; i++) {
                if (possibleLocations.isEmpty())
                    break;

                Location location = possibleLocations.get(this.random.nextInt(possibleLocations.size()));
                LivingEntity entity = nmsHandler.createNewEntityUnspawned(this.entityType, location, CreatureSpawnEvent.SpawnReason.SPAWNER);
                SpawnerFlagPersistenceHook.flagSpawnerSpawned(entity);

                if (ageable)
                    ((Ageable) entity).setAdult();

                if (stackedSpawner.getStackSettings().isMobAIDisabled() && (!Setting.SPAWNER_DISABLE_MOB_AI_ONLY_PLAYER_PLACED.getBoolean() || stackedSpawner.isPlacedByPlayer()))
                    PersistentDataUtils.removeEntityAi(entity);
                PersistentDataUtils.tagSpawnedFromSpawner(entity);

                entityStackSettings.applySpawnerSpawnedProperties(entity);

                StackedEntity newStack = new StackedEntity(entity);
                Optional<StackedEntity> matchingEntity = stackedEntities.stream().filter(x ->
                        entityStackSettings.testCanStackWith(x, newStack, false, true)).findFirst();
                if (matchingEntity.isPresent()) {
                    matchingEntity.get().increaseStackSize(entity);
                } else {
                    stackedEntities.add(newStack);
                    newStacks.add(newStack);
                    possibleLocations.remove(location);
                }

                successfulSpawns++;
            }

            Bukkit.getScheduler().runTask(RoseStacker.getInstance(), () -> {
                stackManager.setEntityStackingTemporarilyDisabled(true);
                for (StackedEntity stackedEntity : newStacks) {
                    LivingEntity entity = stackedEntity.getEntity();

                    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, (CreatureSpawner) stackedSpawner.getBlock().getState());
                    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                    if (spawnerSpawnEvent.isCancelled())
                        continue;

                    nmsHandler.spawnExistingEntity(stackedEntity.getEntity(), CreatureSpawnEvent.SpawnReason.SPAWNER);
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

            Bukkit.getScheduler().runTask(RoseStacker.getInstance(), () -> {
                for (int i = 0; i < spawnAmount; i++) {
                    if (possibleLocations.isEmpty())
                        break;

                    Location location = possibleLocations.remove(this.random.nextInt(possibleLocations.size()));
                    World world = location.getWorld();
                    if (world == null)
                        continue;

                    LivingEntity entity = (LivingEntity) world.spawnEntity(location, this.entityType);
                    entityStackSettings.applySpawnerSpawnedProperties(entity);
                    SpawnerFlagPersistenceHook.flagSpawnerSpawned(entity);

                    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, (CreatureSpawner) stackedSpawner.getBlock().getState());
                    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                    if (spawnerSpawnEvent.isCancelled()) {
                        entity.remove();
                        continue;
                    }

                    // Spawn Particles
                    if (entity.isValid())
                        entity.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, entity.getLocation().clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);
                }
            });
        }

        return successfulSpawns;
    }

}
