package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings.InvalidSpawnCondition;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings.SpawnConditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.scheduler.BukkitTask;

public class SpawnerSpawnManager extends Manager implements Runnable {

    /**
     * At what point should we override the normal spawner spawning?
     */
    public static final int DELAY_THRESHOLD = 3;

    /**
     * How many times should we fail to spawn before giving up?
     */
    private static final int MAX_FAILED_SPAWN_ATTEMPTS = 50;

    private Random random;
    private BukkitTask task;

    public SpawnerSpawnManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.random = new Random();
    }

    @Override
    public void reload() {
        if (!this.roseStacker.getManager(StackManager.class).isSpawnerStackingEnabled())
            return;

        this.task = Bukkit.getScheduler().runTaskTimer(this.roseStacker, this, 0, 1);
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
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);

        boolean randomizeSpawnAmounts = Setting.SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED.getBoolean();

        for (Block block : stackManager.getStackedSpawners().keySet()) {
            if (block.getType() != Material.SPAWNER)
                continue;

            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            if (spawner.getDelay() >= DELAY_THRESHOLD)
                continue;

            EntityType entityType = spawner.getSpawnedType();
            if (entityType.getEntityClass() == null || !LivingEntity.class.isAssignableFrom(entityType.getEntityClass()))
                return;

            StackedSpawner stackedSpawner = stackManager.getStackedSpawners().get(block);
            SpawnerStackSettings stackSettings = stackedSpawner.getStackSettings();
            SpawnConditions spawnConditions = stackSettings.getSpawnConditions();

            // Reset the spawn delay
            int newDelay = this.random.nextInt(spawner.getMaxSpawnDelay() - spawner.getMinSpawnDelay() + 1) + spawner.getMinSpawnDelay();
            spawner.setDelay(newDelay);
            spawner.update();

            // Spawn particles indicating the spawn occurred
            block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);

            List<InvalidSpawnCondition> invalidSpawnConditions = new ArrayList<>();

            boolean meetsEntityConstraint = true;
            boolean hasValidBiome = true;
            boolean hasValidSpawnLocation = false;
            boolean hasValidLightLevel = false;

            int spawnRange = spawner.getSpawnRange();
            if (block.getWorld().getNearbyEntities(block.getLocation().clone().add(0.5, 0.5, 0.5), spawnRange, spawnRange, spawnRange, entity -> entity.getType() == entityType).size() > spawner.getMaxNearbyEntities()) {
                invalidSpawnConditions.add(InvalidSpawnCondition.ENTITY_CAP);
                meetsEntityConstraint = false;
            }

            if (!spawnConditions.getSpawnBiomes().isEmpty() && !spawnConditions.getSpawnBiomes().contains(block.getBiome())) {
                invalidSpawnConditions.add(InvalidSpawnCondition.SPAWN_BIOME);
                hasValidBiome = false;
            }

            // Spawn the mobs
            int spawnAmount;
            if (randomizeSpawnAmounts) {
                int minSpawnAmount = spawner.getSpawnCount() / stackSettings.getSpawnCountStackSizeMultiplier();
                spawnAmount = this.random.nextInt(spawner.getSpawnCount() - minSpawnAmount + 1) + minSpawnAmount;
            } else {
                spawnAmount = spawner.getSpawnCount();
            }

            boolean successfulSpawn = false;
            for (int i = 0; i < spawnAmount; i++) {
                int attempts = 0;
                while (attempts < MAX_FAILED_SPAWN_ATTEMPTS) {
                    int xOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                    int yOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                    int zOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;

                    Location spawnLocation = block.getLocation().clone().add(xOffset + 0.5, yOffset, zOffset + 0.5);

                    Block target = spawnLocation.getBlock();
                    Block below = target.getRelative(BlockFace.DOWN);
                    Block above = target.getRelative(BlockFace.UP);

                    boolean currentSpawnValid = meetsEntityConstraint && hasValidBiome;
                    if ((spawnConditions.getSpawnBlocks().contains(Material.AIR)
                            || spawnConditions.getSpawnBlocks().contains(below.getType()))
                            && ((target.isPassable() || target.isEmpty()) && (above.isPassable() || above.isEmpty()))) {
                        hasValidSpawnLocation = true;
                    } else {
                        currentSpawnValid = false;
                    }

                    switch (spawnConditions.getRequiredLightLevel()) {
                        case LIGHT:
                            if (target.getLightLevel() > 7) {
                                hasValidLightLevel = true;
                            } else {
                                currentSpawnValid = false;
                            }
                            break;
                        case DARK:
                            if (target.getLightLevel() <= 7) {
                                hasValidLightLevel = true;
                            } else {
                                currentSpawnValid = false;
                            }
                            break;
                        case ANY:
                            hasValidLightLevel = true;
                            break;
                    }

                    if (currentSpawnValid) {
                        Entity entity = block.getWorld().spawnEntity(spawnLocation, entityType);

                        SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(entity, spawner);
                        Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
                        if (spawnerSpawnEvent.isCancelled())
                            entity.remove();

                        if (entity.isValid()) // Don't spawn particles for auto-stacked entities
                            block.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, spawnLocation.clone().add(0, 0.75, 0), 5, 0.25, 0.25, 0.25, 0.01);

                        successfulSpawn = true;
                        break;
                    }

                    attempts++;
                }
            }

            if (!successfulSpawn) {
                if (!hasValidLightLevel)
                    invalidSpawnConditions.add(InvalidSpawnCondition.LIGHT_LEVEL);
                if (!hasValidSpawnLocation)
                    invalidSpawnConditions.add(InvalidSpawnCondition.SPAWN_BLOCK);
            }

            stackedSpawner.getLastInvalidConditions().clear();
            if (!successfulSpawn)
                stackedSpawner.getLastInvalidConditions().addAll(invalidSpawnConditions);
        }
    }

}
