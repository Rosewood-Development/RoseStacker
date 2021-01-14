package dev.rosewood.rosestacker.nms.v1_13_R2.object;

import dev.rosewood.rosestacker.nms.object.SpawnerTileWrapper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.MobSpawnerAbstract;
import net.minecraft.server.v1_13_R2.MobSpawnerData;
import net.minecraft.server.v1_13_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_13_R2.WorldServer;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;

public class SpawnerTileWrapperImpl implements SpawnerTileWrapper {

    private static Field field_MobSpawnerAbstract_mobs;
    static {
        try {
            field_MobSpawnerAbstract_mobs = MobSpawnerAbstract.class.getDeclaredField("mobs");
            field_MobSpawnerAbstract_mobs.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

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

    @SuppressWarnings("unchecked")
    @Override
    public List<EntityType> getSpawnedTypes() {
        List<MobSpawnerData> mobs;
        try {
            mobs = (List<MobSpawnerData>) field_MobSpawnerAbstract_mobs.get(this.spawner);
        } catch (ReflectiveOperationException ex) {
            mobs = new ArrayList<>();
        }

        return mobs.stream()
                .map(x -> x.b().getString("id"))
                .map(MinecraftKey::a)
                .filter(Objects::nonNull)
                .map(CraftNamespacedKey::fromMinecraft)
                .map(this::fromKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private EntityType fromKey(NamespacedKey namespacedKey) {
        return Arrays.stream(EntityType.values())
                .filter(x -> x != EntityType.UNKNOWN)
                .filter(x -> NamespacedKey.minecraft(x.getName()).equals(namespacedKey))
                .findFirst()
                .orElse(null);
    }

    private void update() {
        this.tileEntity.update();
        this.world.notify(this.tileEntity.getPosition(), this.tileEntity.getBlock(), this.tileEntity.getBlock(), 3);
    }

}
