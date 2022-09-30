package dev.rosewood.rosestacker.nms.v1_16_R3.spawner;

import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.spawner.spawning.MobSpawningMethod;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.Arrays;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.MobSpawnerData;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R3.WeightedRandom;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;

public class StackedSpawnerTileImpl extends MobSpawnerAbstract implements StackedSpawnerTile {

    private final TileEntityMobSpawner blockEntity;
    private final BlockPosition blockPos;
    private final StackedSpawner stackedSpawner;
    private boolean redstoneDeactivated;
    private int redstoneTimeSinceLastCheck;
    private boolean playersNearby;
    private int playersTimeSinceLastCheck;
    private boolean checkedInitialConditions;

    public StackedSpawnerTileImpl(MobSpawnerAbstract old, TileEntityMobSpawner blockEntity, StackedSpawner stackedSpawner) {
        this.blockEntity = blockEntity;
        this.stackedSpawner = stackedSpawner;
        Location location = stackedSpawner.getLocation();
        this.blockPos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.loadOld(old);
    }

    @Override
    public void c() {
        World level = this.a();
        if (level == null)
            return;

        // Only tick the spawner if a player is nearby
        this.playersTimeSinceLastCheck = (this.playersTimeSinceLastCheck + 1) % Setting.SPAWNER_PLAYER_CHECK_FREQUENCY.getInt();
        if (this.playersTimeSinceLastCheck == 0)
            this.playersNearby = this.isNearPlayer(level, this.blockPos);

        if (!this.playersNearby)
            return;

        if (!this.checkedInitialConditions) {
            this.checkedInitialConditions = true;
            this.trySpawns(true);
        }

        SpawnerStackSettings stackSettings = this.stackedSpawner.getStackSettings();

        // Handle redstone deactivation if enabled
        if (Setting.SPAWNER_DEACTIVATE_WHEN_POWERED.getBoolean()) {
            if (this.redstoneTimeSinceLastCheck == 0) {
                boolean hasSignal = level.isBlockIndirectlyPowered(this.blockPos);
                if (this.redstoneDeactivated && !hasSignal) {
                    this.redstoneDeactivated = false;
                    this.requiredPlayerRange = stackSettings.getPlayerActivationRange();
                    this.updateTile();
                } else if (!this.redstoneDeactivated && hasSignal) {
                    this.redstoneDeactivated = true;
                    this.requiredPlayerRange = 0;
                    this.updateTile();
                }

                if (this.redstoneDeactivated)
                    return;
            }

            this.redstoneTimeSinceLastCheck = (this.redstoneTimeSinceLastCheck + 1) % Setting.SPAWNER_POWERED_CHECK_FREQUENCY.getInt();
        }

        // Count down spawn timer unless we are ready to spawn
        if (this.spawnDelay > 0) {
            this.spawnDelay--;
            return;
        }

        // Reset spawn delay
        this.spawnDelay = level.getRandom().nextInt(this.maxSpawnDelay - this.minSpawnDelay + 1) + this.minSpawnDelay;
        this.updateTile();

        // Execute spawning method
        this.trySpawns(false);

        // Randomize spawn potentials
        if (!this.mobs.isEmpty())
            this.setSpawnData(WeightedRandom.a(this.a().random, this.mobs));
    }

    private void trySpawns(boolean onlyCheckConditions) {
        try {
            if (this.spawnData != null) {
                MinecraftKey resourceLocation = MinecraftKey.a(this.spawnData.getEntity().getString("id"));
                if (resourceLocation != null) {
                    NamespacedKey namespacedKey = CraftNamespacedKey.fromMinecraft(resourceLocation);
                    EntityType entityType = this.fromKey(namespacedKey);
                    if (entityType != null)
                        new MobSpawningMethod(entityType).spawn(this.stackedSpawner, onlyCheckConditions);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EntityType fromKey(NamespacedKey namespacedKey) {
        return Arrays.stream(EntityType.values())
                .filter(x -> x != EntityType.UNKNOWN)
                .filter(x -> x.getKey().equals(namespacedKey))
                .findFirst()
                .orElse(null);
    }

    private void updateTile() {
        World level = this.a();
        if (level != null) {
            level.b(this.blockPos, this.blockEntity);
            IBlockData var1 = this.a().getType(this.b());
            this.a().notify(this.blockPos, var1, var1, 3);
        }
    }

    @Override
    public void a(int var0) {
        this.a().playBlockAction(this.b(), Blocks.SPAWNER, var0, 0);
    }

    @Override
    public World a() {
        return this.blockEntity.getWorld();
    }

    @Override
    public BlockPosition b() {
        return this.blockPos;
    }

    @Override
    public void setSpawnData(MobSpawnerData var0) {
        super.setSpawnData(var0);
        if (this.a() != null) {
            IBlockData var1 = this.a().getType(this.b());
            this.a().notify(this.blockPos, var1, var1, 4);
        }
    }

    private boolean isNearPlayer(World level, BlockPosition blockPos) {
        if (this.requiredPlayerRange < 0)
            return true;
        return level.isPlayerNearby((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, Math.max(this.stackedSpawner.getStackSettings().getPlayerActivationRange(), 0.1));
    }

    private void loadOld(MobSpawnerAbstract baseSpawner) {
        this.spawnDelay = baseSpawner.spawnDelay;
        this.mobs.clear();
        this.mobs.addAll(baseSpawner.mobs);
        this.spawnData = baseSpawner.spawnData;
        this.minSpawnDelay = baseSpawner.minSpawnDelay;
        this.maxSpawnDelay = baseSpawner.maxSpawnDelay;
        this.spawnCount = baseSpawner.spawnCount;
        this.maxNearbyEntities = baseSpawner.maxNearbyEntities;
        this.requiredPlayerRange = baseSpawner.requiredPlayerRange;
        this.spawnRange = baseSpawner.spawnRange;
        this.updateTile();
    }

    @Override
    public EntityType getSpawnedType() {
        MinecraftKey resourceLocation = MinecraftKey.a(this.spawnData.getEntity().getString("id"));
        if (resourceLocation != null) {
            NamespacedKey namespacedKey = CraftNamespacedKey.fromMinecraft(resourceLocation);
            EntityType entityType = this.fromKey(namespacedKey);
            if (entityType != null)
                return entityType;
        }
        return EntityType.PIG;
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        this.spawnData.getEntity().setString("id", entityType.getKey().getKey());
        this.mobs.clear();
    }

    @Override
    public int getDelay() {
        return this.spawnDelay;
    }

    @Override
    public void setDelay(int delay) {
        this.spawnDelay = delay;
        this.updateTile();
    }

    @Override
    public int getMinSpawnDelay() {
        return this.minSpawnDelay;
    }

    @Override
    public void setMinSpawnDelay(int delay) {
        this.minSpawnDelay = delay;
        this.updateTile();
    }

    @Override
    public int getMaxSpawnDelay() {
        return this.maxSpawnDelay;
    }

    @Override
    public void setMaxSpawnDelay(int delay) {
        this.maxSpawnDelay = delay;
        this.updateTile();
    }

    @Override
    public int getSpawnCount() {
        return this.spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
        this.updateTile();
    }

    @Override
    public int getMaxNearbyEntities() {
        return this.maxNearbyEntities;
    }

    @Override
    public void setMaxNearbyEntities(int maxNearbyEntities) {
        this.maxNearbyEntities = maxNearbyEntities;
        this.updateTile();
    }

    @Override
    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        this.requiredPlayerRange = requiredPlayerRange;
        this.updateTile();
    }

    @Override
    public int getSpawnRange() {
        return this.spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        this.spawnRange = spawnRange;
        this.updateTile();
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.blockEntity.persistentDataContainer;
    }

}
