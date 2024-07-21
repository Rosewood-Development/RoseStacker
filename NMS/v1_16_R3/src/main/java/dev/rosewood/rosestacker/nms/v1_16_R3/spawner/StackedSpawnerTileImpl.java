package dev.rosewood.rosestacker.nms.v1_16_R3.spawner;

import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.nms.util.ExtraUtils;
import dev.rosewood.rosestacker.spawning.MobSpawningMethod;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.MobSpawnerData;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R3.WeightedRandom;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
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
        this.playersTimeSinceLastCheck = (this.playersTimeSinceLastCheck + 1) % SettingKey.SPAWNER_PLAYER_CHECK_FREQUENCY.get();
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
        if (SettingKey.SPAWNER_DEACTIVATE_WHEN_POWERED.get()) {
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

            this.redstoneTimeSinceLastCheck = (this.redstoneTimeSinceLastCheck + 1) % SettingKey.SPAWNER_POWERED_CHECK_FREQUENCY.get();
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
                String typeId = this.spawnData.getEntity().getString("id");
                if (!typeId.isEmpty()) {
                    EntityType entityType = ExtraUtils.getEntityTypeFromKey(NamespacedKey.fromString(typeId));
                    if (entityType != null)
                        new MobSpawningMethod(entityType).spawn(this.stackedSpawner, onlyCheckConditions);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (this.stackedSpawner.getStackSettings().hasUnlimitedPlayerActivationRange())
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
    public SpawnerType getSpawnerType() {
        if (this.mobs.isEmpty()) {
            if (this.spawnData == null)
                return SpawnerType.empty();

            String typeId = this.spawnData.getEntity().getString("id");
            if (typeId.isEmpty())
                return SpawnerType.empty();

            return SpawnerType.of(ExtraUtils.getEntityTypeFromKey(NamespacedKey.fromString(typeId)));
        }

        return SpawnerType.of(this.mobs.stream()
                .map(MobSpawnerData::getEntity)
                .map(x -> x.getString("id"))
                .map(NamespacedKey::fromString)
                .map(ExtraUtils::getEntityTypeFromKey)
                .toList());
    }

    @Override
    public void setSpawnerType(SpawnerType spawnerType) {
        if (spawnerType.size() == 1) {
            this.spawnData = new MobSpawnerData();
            this.spawnData.getEntity().setString("id", spawnerType.getOrThrow().getKey().getKey());
            this.mobs.clear();
            this.updateTile();
            return;
        }

        List<MobSpawnerData> entries = new ArrayList<>();
        for (EntityType entityType : spawnerType.getEntityTypes()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("id", entityType.getKey().getKey());
            entries.add(1, new MobSpawnerData(tag));
        }
        this.mobs.addAll(entries);

        if (!this.mobs.isEmpty())
            this.setSpawnData(WeightedRandom.a(this.a().random, this.mobs));

        this.updateTile();
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
