package dev.rosewood.rosestacker.nms.v1_17_R1.object;

import dev.rosewood.rosestacker.nms.object.SettingFetcher;
import dev.rosewood.rosestacker.nms.object.StackedSpawnerTile;
import dev.rosewood.rosestacker.spawner.spawning.MobSpawningMethod;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;

public class StackedSpawnerTileImpl extends BaseSpawner implements StackedSpawnerTile {

    private final SpawnerBlockEntity blockEntity;
    private final SettingFetcher settingFetcher;
    private final BlockPos blockPos;
    private StackedSpawner stackedSpawner;

    private boolean redstoneDeactivated;

    public StackedSpawnerTileImpl(BaseSpawner old, SpawnerBlockEntity blockEntity, StackedSpawner stackedSpawner, SettingFetcher settingFetcher) {
        this.blockEntity = blockEntity;
        this.stackedSpawner = stackedSpawner;
        this.settingFetcher = settingFetcher;
        Location location = stackedSpawner.getLocation();
        this.blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.loadOld(old);
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos blockPos) {
        // Only tick the spawner if a player is nearby
        if (!this.isNearPlayer(level, blockPos))
            return;

        SpawnerStackSettings stackSettings = this.stackedSpawner.getStackSettings();

        // Handle redstone deactivation if enabled
        if (this.settingFetcher.allowSpawnerRedstoneToggle()) {
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

        // TODO: Remove this after the update is finished, this is just to let us know that we have successfully taken over the spawning logic
        this.stackedSpawner.getWorld().spawnParticle(Particle.SPELL_WITCH, this.stackedSpawner.getLocation().clone().add(0.5, 0.5, 0.5), 1);

        // Count down spawn timer unless we are ready to spawn
        if (this.spawnDelay > 0) {
            this.spawnDelay--;
            return;
        }

        // Reset spawn delay
        this.spawnDelay = level.getRandom().nextInt(this.maxSpawnDelay - this.minSpawnDelay + 1) + this.minSpawnDelay;
        this.updateTile();

        // Execute spawning method
        if (this.nextSpawnData != null) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(this.nextSpawnData.getTag().getString("id"));
            if (resourceLocation != null) {
                NamespacedKey namespacedKey = CraftNamespacedKey.fromMinecraft(resourceLocation);
                EntityType entityType = this.fromKey(namespacedKey);
                if (entityType != null)
                    new MobSpawningMethod(entityType).spawn(this.stackedSpawner, this);
            }
        }

        // Randomize spawn potentials
        this.spawnPotentials.getRandom(level.getRandom()).ifPresent(x -> this.nextSpawnData = x);
    }

    private EntityType fromKey(NamespacedKey namespacedKey) {
        return Arrays.stream(EntityType.values())
                .filter(x -> x != EntityType.UNKNOWN)
                .filter(x -> x.getKey().equals(namespacedKey))
                .findFirst()
                .orElse(null);
    }

    private void updateTile() {
        Level level = this.blockEntity.getLevel();
        if (level != null) {
            this.blockEntity.setChanged();
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

    public void updateStackedSpawner(StackedSpawner stackedSpawner) {
        this.stackedSpawner = stackedSpawner;
    }

    @Override
    public EntityType getSpawnedType() {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(this.nextSpawnData.getTag().getString("id"));
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
        this.nextSpawnData.getTag().putString("id", entityType.getKey().getKey());
        this.spawnPotentials = WeightedRandomList.create();
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
