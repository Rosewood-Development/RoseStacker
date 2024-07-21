package dev.rosewood.rosestacker.nms.v1_19_R3.spawner;

import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.nms.util.ExtraUtils;
import dev.rosewood.rosestacker.spawning.MobSpawningMethod;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;

public class StackedSpawnerTileImpl extends BaseSpawner implements StackedSpawnerTile {

    private final SpawnerBlockEntity blockEntity;
    private final BlockPos blockPos;
    private final StackedSpawner stackedSpawner;
    private boolean redstoneDeactivated;
    private int redstoneTimeSinceLastCheck;
    private boolean playersNearby;
    private int playersTimeSinceLastCheck;
    private boolean checkedInitialConditions;

    public StackedSpawnerTileImpl(BaseSpawner old, SpawnerBlockEntity blockEntity, StackedSpawner stackedSpawner) {
        this.blockEntity = blockEntity;
        this.stackedSpawner = stackedSpawner;
        Location location = stackedSpawner.getLocation();
        this.blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.loadOld(old);
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos blockPos) {
        // Only tick the spawner if a player is nearby
        this.playersTimeSinceLastCheck = (this.playersTimeSinceLastCheck + 1) % SettingKey.SPAWNER_PLAYER_CHECK_FREQUENCY.get();
        if (this.playersTimeSinceLastCheck == 0)
            this.playersNearby = this.isNearPlayer(level, blockPos);

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
                boolean hasSignal = level.hasNeighborSignal(this.blockPos);
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
        this.spawnPotentials.getRandom(level.getRandom()).map(WeightedEntry.Wrapper::getData).ifPresent(x -> this.nextSpawnData = x);
    }

    private void trySpawns(boolean onlyCheckConditions) {
        try {
            if (this.nextSpawnData != null) {
                String typeId = this.nextSpawnData.getEntityToSpawn().getString("id");
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
        Level level = this.blockEntity.getLevel();
        if (level != null) {
            level.blockEntityChanged(this.blockPos);
            level.sendBlockUpdated(this.blockPos, this.blockEntity.getBlockState(), this.blockEntity.getBlockState(), 3);
        }
    }

    @Override
    public void broadcastEvent(Level level, BlockPos blockPos, int eventId) {
        level.blockEvent(blockPos, Blocks.SPAWNER, eventId, 0);
    }

    @Override
    public void setNextSpawnData(Level level, BlockPos blockPos, SpawnData spawnData) {
        super.setNextSpawnData(level, blockPos, spawnData);
        if (level != null) {
            BlockState var3 = level.getBlockState(blockPos);
            level.sendBlockUpdated(blockPos, var3, var3, 4);
        }
    }

    private boolean isNearPlayer(Level level, BlockPos blockPos) {
        if (this.stackedSpawner.getStackSettings().hasUnlimitedPlayerActivationRange())
            return true;
        return level.hasNearbyAlivePlayer((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, Math.max(this.stackedSpawner.getStackSettings().getPlayerActivationRange(), 0.1));
    }

    private void loadOld(BaseSpawner baseSpawner) {
        this.spawnDelay = baseSpawner.spawnDelay;
        this.spawnPotentials = baseSpawner.spawnPotentials;
        this.nextSpawnData = baseSpawner.nextSpawnData;
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
        if (this.spawnPotentials.isEmpty()) {
            if (this.nextSpawnData == null)
                return SpawnerType.empty();

            String typeId = this.nextSpawnData.getEntityToSpawn().getString("id");
            if (typeId.isEmpty())
                return SpawnerType.empty();

            return SpawnerType.of(ExtraUtils.getEntityTypeFromKey(NamespacedKey.fromString(typeId)));
        }

        return SpawnerType.of(this.spawnPotentials.unwrap().stream()
                .map(WeightedEntry.Wrapper::getData)
                .map(SpawnData::getEntityToSpawn)
                .map(x -> x.getString("id"))
                .map(NamespacedKey::fromString)
                .map(ExtraUtils::getEntityTypeFromKey)
                .toList());
    }

    @Override
    public void setSpawnerType(SpawnerType spawnerType) {
        if (spawnerType.size() == 1) {
            this.nextSpawnData = new SpawnData();
            this.nextSpawnData.getEntityToSpawn().putString("id", spawnerType.getOrThrow().getKey().getKey());
            this.spawnPotentials = SimpleWeightedRandomList.empty();
            this.updateTile();
            return;
        }

        SimpleWeightedRandomList.Builder<SpawnData> builder = SimpleWeightedRandomList.builder();
        for (EntityType entityType : spawnerType.getEntityTypes()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entityType.getKey().getKey());
            builder.add(new SpawnData(tag, Optional.empty()), 1);
        }
        this.spawnPotentials = builder.build();

        this.spawnPotentials.getRandom(RandomSource.create()).map(WeightedEntry.Wrapper::getData).ifPresent(x -> this.nextSpawnData = x);
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
