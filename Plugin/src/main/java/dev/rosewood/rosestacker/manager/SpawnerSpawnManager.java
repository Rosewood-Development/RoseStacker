package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.tags.NoneConditionTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
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
        int maxFailedSpawnAttempts = Setting.SPAWNER_MAX_FAILED_SPAWN_ATTEMPTS.getInt();

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

            // Reset the spawn delay
            int newDelay = this.random.nextInt(spawner.getMaxSpawnDelay() - spawner.getMinSpawnDelay() + 1) + spawner.getMinSpawnDelay();
            spawner.setDelay(newDelay);
            spawner.update();

            // Spawn particles indicating the spawn occurred
            block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);

            List<ConditionTag> spawnRequirements = new ArrayList<>(stackSettings.getSpawnRequirements());

            // Check general spawner conditions
            List<ConditionTag> perSpawnConditions = spawnRequirements.stream().filter(ConditionTag::isRequiredPerSpawn).collect(Collectors.toList());
            spawnRequirements.removeAll(perSpawnConditions);

            Set<ConditionTag> invalidSpawnConditions = spawnRequirements.stream().filter(x -> !x.check(spawner, spawner.getBlock())).collect(Collectors.toSet());
            boolean passedSpawnerChecks = invalidSpawnConditions.isEmpty();

            invalidSpawnConditions.addAll(perSpawnConditions); // Will be removed when they pass

            // Spawn the mobs
            int spawnAmount;
            if (randomizeSpawnAmounts) {
                int minSpawnAmount = spawner.getSpawnCount() / stackSettings.getSpawnCountStackSizeMultiplier();
                spawnAmount = this.random.nextInt(spawner.getSpawnCount() - minSpawnAmount + 1) + minSpawnAmount;
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

}
