package dev.rosewood.rosestacker.nms.v1_15_R1;

import com.google.common.collect.Lists;
import dev.rosewood.rosestacker.nms.NMSHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.DataWatcher;
import net.minecraft.server.v1_15_R1.DataWatcherObject;
import net.minecraft.server.v1_15_R1.DataWatcherRegistry;
import net.minecraft.server.v1_15_R1.DataWatcherSerializer;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityCreeper;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagDouble;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SuppressWarnings("unchecked")
public class NMSHandlerImpl implements NMSHandler {

    private static Method method_EntityLiving_a; // Method to update the EntityLiving LootTable, normally protected
    private static Field field_PacketPlayOutEntityMetadata_a; // Field to set the entity ID for the packet, normally private
    private static Field field_PacketPlayOutEntityMetadata_b; // Field to set the datawatcher changes for the packet, normally private

    private static DataWatcherObject<Boolean> value_EntityCreeper_d; // DataWatcherObject that determines if a creeper is ignited, normally private
    private static Field field_EntityCreeper_fuseTicks; // Field to set the remianing fuse ticks of a creeper, normally private

    static {
        try {
            method_EntityLiving_a = EntityLiving.class.getDeclaredMethod("a", DamageSource.class, boolean.class);
            method_EntityLiving_a.setAccessible(true);

            field_PacketPlayOutEntityMetadata_a = PacketPlayOutEntityMetadata.class.getDeclaredField("a");
            field_PacketPlayOutEntityMetadata_a.setAccessible(true);

            field_PacketPlayOutEntityMetadata_b = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
            field_PacketPlayOutEntityMetadata_b.setAccessible(true);

            Field field_EntityCreeper_d = EntityCreeper.class.getDeclaredField("d");
            field_EntityCreeper_d.setAccessible(true);
            value_EntityCreeper_d = (DataWatcherObject<Boolean>) field_EntityCreeper_d.get(null);

            field_EntityCreeper_fuseTicks = EntityCreeper.class.getDeclaredField("fuseTicks");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getEntityAsNBT(LivingEntity livingEntity, boolean includeAttributes) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            NBTTagCompound nbt = new NBTTagCompound();
            EntityLiving craftEntity = ((CraftLivingEntity) livingEntity).getHandle();
            craftEntity.save(nbt);

            // Don't store attributes, it's pretty large and doesn't usually matter
            if (!includeAttributes)
                nbt.remove("Attributes");

            // Write entity type
            String entityType = IRegistry.ENTITY_TYPE.getKey(craftEntity.getEntityType()).toString();
            dataOutput.writeUTF(entityType);

            // Write NBT
            NBTCompressedStreamTools.a(nbt, (OutputStream) dataOutput);

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public LivingEntity spawnEntityFromNBT(byte[] serialized, Location location) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(serialized);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read entity type
            String entityType = dataInput.readUTF();

            // Read NBT
            NBTTagCompound nbt = NBTCompressedStreamTools.a(dataInput);

            Optional<EntityTypes<?>> optionalEntity = EntityTypes.a(entityType);
            if (optionalEntity.isPresent()) {
                Entity spawned = optionalEntity.get().spawnCreature(
                        ((CraftWorld) location.getWorld()).getHandle(),
                        nbt,
                        null,
                        null,
                        new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                        EnumMobSpawn.COMMAND,
                        true,
                        false
                );

                if (spawned != null) {
                    this.setNBT(spawned, nbt, location);
                    return (LivingEntity) spawned.getBukkitEntity();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public LivingEntity getNBTAsEntity(EntityType entityType, Location location, byte[] serialized) {
        if (location.getWorld() == null)
            return null;

        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        EntityLiving entity = (EntityLiving) craftWorld.createEntity(location, entityType.getEntityClass());

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(serialized);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read entity type, don't need the value
            dataInput.readUTF();

            // Read NBT
            NBTTagCompound nbt = NBTCompressedStreamTools.a(dataInput);

            // Set NBT
            this.setNBT(entity, nbt, location);

            // Update loot table
            method_EntityLiving_a.invoke(entity, DamageSource.GENERIC, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (LivingEntity) entity.getBukkitEntity();
    }

    @Override
    public LivingEntity createEntityUnspawned(EntityType entityType, Location location) {
        if (location.getWorld() == null)
            return null;

        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        return (LivingEntity) craftWorld.createEntity(location, entityType.getEntityClass()).getBukkitEntity();
    }

    @Override
    public void toggleEntityNameTagForPlayer(Player player, org.bukkit.entity.Entity entity, boolean visible) {
        try {
            DataWatcherSerializer<Boolean> dataWatcherSerializer = DataWatcherRegistry.i;
            DataWatcher.Item<Boolean> dataWatcherItem = new DataWatcher.Item<>(dataWatcherSerializer.a(3), visible);

            PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata();
            field_PacketPlayOutEntityMetadata_a.set(packetPlayOutEntityMetadata, entity.getEntityId());
            field_PacketPlayOutEntityMetadata_b.set(packetPlayOutEntityMetadata, Lists.newArrayList(dataWatcherItem));

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutEntityMetadata);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unigniteCreeper(Creeper creeper) {
        EntityCreeper entityCreeper = ((CraftCreeper) creeper).getHandle();

        entityCreeper.getDataWatcher().set(value_EntityCreeper_d, false);
        try {
            field_EntityCreeper_fuseTicks.set(entityCreeper, entityCreeper.maxFuseTicks);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the NBT data for an entity
     *
     * @param entity The entity to apply NBT data to
     * @param nbt The NBT data
     * @param desiredLocation The location of the entity
     */
    private void setNBT(Entity entity, NBTTagCompound nbt, Location desiredLocation) {
        NBTTagList nbtTagList = nbt.getList("Pos", 6);
        nbtTagList.set(0, NBTTagDouble.a(desiredLocation.getX()));
        nbtTagList.set(1, NBTTagDouble.a(desiredLocation.getY()));
        nbtTagList.set(2, NBTTagDouble.a(desiredLocation.getZ()));
        nbt.set("Pos", nbtTagList);
        entity.f(nbt);
    }

}
