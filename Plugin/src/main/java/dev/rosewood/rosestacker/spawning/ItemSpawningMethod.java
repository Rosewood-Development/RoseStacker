package dev.rosewood.rosestacker.spawning;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NoneConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.tags.NotPlayerPlacedConditionTag;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import dev.rosewood.rosestacker.utils.VersionUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class ItemSpawningMethod implements SpawningMethod {

    private final Material itemType;
    private final Random random;

    public ItemSpawningMethod(Material itemType) {
        this.itemType = itemType;
        this.random = new Random();
    }

    @Override
    public void spawn(StackedSpawner stackedSpawner, boolean onlyCheckConditions) {
        StackedSpawnerTile spawnerTile = stackedSpawner.getSpawnerTile();
        SpawnerStackSettings stackSettings = stackedSpawner.getStackSettings();

        // Mob spawning logic
        List<ConditionTag> spawnRequirements = new ArrayList<>();

        // Check general spawner conditions // TODO
        List<ConditionTag> perSpawnConditions = spawnRequirements.stream().filter(ConditionTag::isRequiredPerSpawn).toList();
        spawnRequirements.removeAll(perSpawnConditions);

        Set<ConditionTag> invalidSpawnConditions = spawnRequirements.stream().filter(x -> !x.check(stackedSpawner, stackedSpawner.getBlock())).collect(Collectors.toSet());
        if (SettingKey.SPAWNER_SPAWN_ONLY_PLAYER_PLACED.get() && !stackedSpawner.isPlacedByPlayer())
            invalidSpawnConditions.add(NotPlayerPlacedConditionTag.INSTANCE);

        boolean passedSpawnerChecks = invalidSpawnConditions.isEmpty();

        invalidSpawnConditions.addAll(perSpawnConditions); // Will be removed when they pass

        // Spawn the items
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

        ThreadUtils.runAsync(() -> {
            Set<Location> spawnLocations = new HashSet<>();
            int spawnRange = spawnerTile.getSpawnRange();
            for (int i = 0; i < spawnAmount; i++) {
                int attempts = 0;
                while (attempts < SettingKey.SPAWNER_MAX_FAILED_SPAWN_ATTEMPTS.get()) {
                    int xOffset = this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
                    int yOffset = !SettingKey.SPAWNER_USE_VERTICAL_SPAWN_RANGE.get() ? this.random.nextInt(3) - 1 : this.random.nextInt(spawnRange * 2 + 1) - spawnRange;
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

            int successfulSpawns = spawnLocations.size() > 0 ? spawnAmount : 0;

            // Drop items
            ThreadUtils.runSync(() -> {
                // Assign each location a portion of the total items to drop
                int amountPerLocation = (int) Math.ceil((double) spawnAmount / spawnLocations.size());
                int amountLeft = spawnAmount;
                for (Location location : spawnLocations) {
                    int amount = Math.min(amountPerLocation, amountLeft);
                    amountLeft -= amount;
                    for (ItemStack itemStack : GuiUtil.getMaterialAmountAsItemStacks(this.itemType, amount))
                        stackedSpawner.getWorld().dropItemNaturally(location.clone().add(0.5, 0.5, 0.5), itemStack);
                    stackedSpawner.getWorld().spawnParticle(VersionUtils.POOF, location.clone().add(0, 0.75, 0), 2, 0.25, 0.25, 0.25, 0.01);
                }
            });

            stackedSpawner.getLastInvalidConditions().clear();
            if (successfulSpawns == 0) {
                if (invalidSpawnConditions.isEmpty()) {
                    stackedSpawner.getLastInvalidConditions().add(NoneConditionTag.class);
                } else {
                    List<Class<? extends ConditionTag>> invalidSpawnConditionClasses = new ArrayList<>();
                    for (ConditionTag conditionTag : invalidSpawnConditions)
                        invalidSpawnConditionClasses.add(conditionTag.getClass());
                    stackedSpawner.getLastInvalidConditions().addAll(invalidSpawnConditionClasses);
                }

                // Spawn particles indicating the spawn did not occur
                stackedSpawner.getWorld().spawnParticle(VersionUtils.SMOKE, stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
            } else {
                // Spawn particles indicating the spawn occurred
                stackedSpawner.getWorld().spawnParticle(Particle.FLAME, stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0);
                ThreadUtils.runSync(() -> {
                    if (stackedSpawner.getBlock().getType() == Material.SPAWNER)
                        PersistentDataUtils.increaseSpawnCount(spawnerTile, successfulSpawns);
                });
            }
        });
    }

}
