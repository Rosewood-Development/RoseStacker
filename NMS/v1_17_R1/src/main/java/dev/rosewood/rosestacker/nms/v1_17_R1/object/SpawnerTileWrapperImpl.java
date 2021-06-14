package dev.rosewood.rosestacker.nms.v1_17_R1.object;

import dev.rosewood.rosestacker.nms.object.SpawnerTileWrapper;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;

public class SpawnerTileWrapperImpl implements SpawnerTileWrapper {

    private final SpawnerBlockEntity tileEntity;
    private final BaseSpawner spawner;
    private final ServerLevel world;

    public SpawnerTileWrapperImpl(CreatureSpawner spawner) {
        CraftBlock block = (CraftBlock) spawner.getBlock();
        this.world = block.getCraftWorld().getHandle();
        this.tileEntity = (SpawnerBlockEntity) this.world.getTileEntity(block.getPosition(), true);
        if (this.tileEntity == null)
            throw new IllegalStateException("CreatureSpawner at " + spawner.getLocation() + " no longer exists!");

        this.spawner = this.tileEntity.getSpawner();
    }

    @Override
    public int getDelay() {
        return this.spawner.spawnDelay;
    }

    @Override
    public void setDelay(int delay) {
        this.spawner.spawnDelay = delay;
        this.update();
    }

    @Override
    public int getMinSpawnDelay() {
        return this.spawner.minSpawnDelay;
    }

    @Override
    public void setMinSpawnDelay(int delay) {
        this.spawner.minSpawnDelay = delay;
        this.update();
    }

    @Override
    public int getMaxSpawnDelay() {
        return this.spawner.maxSpawnDelay;
    }

    @Override
    public void setMaxSpawnDelay(int delay) {
        this.spawner.maxSpawnDelay = delay;
        this.update();
    }

    @Override
    public int getSpawnCount() {
        return this.spawner.spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        this.spawner.spawnCount = spawnCount;
        this.update();
    }

    @Override
    public int getMaxNearbyEntities() {
        return this.spawner.maxNearbyEntities;
    }

    @Override
    public void setMaxNearbyEntities(int maxNearbyEntities) {
        this.spawner.maxNearbyEntities = maxNearbyEntities;
        this.update();
    }

    @Override
    public int getRequiredPlayerRange() {
        return this.spawner.requiredPlayerRange;
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        this.spawner.requiredPlayerRange = requiredPlayerRange;
        this.update();
    }

    @Override
    public int getSpawnRange() {
        return this.spawner.spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        this.spawner.spawnRange = spawnRange;
        this.update();
    }

    @Override
    public List<EntityType> getSpawnedTypes() {
        return this.spawner.spawnPotentials.unwrap().stream()
                .map(x -> x.getTag().getString("id"))
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(CraftNamespacedKey::fromMinecraft)
                .map(this::fromKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private EntityType fromKey(NamespacedKey namespacedKey) {
        return Arrays.stream(EntityType.values())
                .filter(x -> x != EntityType.UNKNOWN)
                .filter(x -> x.getKey().equals(namespacedKey))
                .findFirst()
                .orElse(null);
    }

    public void update() {
        this.tileEntity.setChanged();
        this.world.sendBlockUpdated(this.tileEntity.getBlockPos(), this.tileEntity.getBlockState(), this.tileEntity.getBlockState(), 3);
    }

    public BaseSpawner getHandle() {
        return this.spawner;
    }

}
