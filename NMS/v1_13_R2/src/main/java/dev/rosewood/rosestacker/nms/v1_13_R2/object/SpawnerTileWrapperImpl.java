package dev.rosewood.rosestacker.nms.v1_13_R2.object;

import dev.rosewood.rosestacker.nms.object.SpawnerTileWrapper;
import net.minecraft.server.v1_13_R2.MobSpawnerAbstract;
import net.minecraft.server.v1_13_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_13_R2.WorldServer;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;

public class SpawnerTileWrapperImpl implements SpawnerTileWrapper {

    private final TileEntityMobSpawner tileEntity;
    private final MobSpawnerAbstract spawner;
    private final WorldServer world;

    public SpawnerTileWrapperImpl(CreatureSpawner spawner) {
        CraftBlock block = (CraftBlock) spawner.getBlock();
        this.world = block.getCraftWorld().getHandle();
        this.tileEntity = (TileEntityMobSpawner) this.world.getTileEntity(block.getPosition());
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

    private void update() {
        this.tileEntity.update();
        this.world.notify(this.tileEntity.getPosition(), this.tileEntity.getBlock(), this.tileEntity.getBlock(), 3);
    }

}
