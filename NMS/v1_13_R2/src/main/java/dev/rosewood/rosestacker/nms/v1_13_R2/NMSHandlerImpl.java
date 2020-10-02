package dev.rosewood.rosestacker.nms.v1_13_R2;

import com.google.common.collect.Lists;
import dev.rosewood.rosestacker.nms.NMSHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.DataWatcher;
import net.minecraft.server.v1_13_R2.DataWatcherObject;
import net.minecraft.server.v1_13_R2.DataWatcherRegistry;
import net.minecraft.server.v1_13_R2.DataWatcherSerializer;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityCreeper;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagDouble;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_13_R2.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SuppressWarnings("unchecked")
public class NMSHandlerImpl implements NMSHandler {

    private static Field field_PacketPlayOutEntityMetadata_a; // Field to set the entity ID for the packet, normally private
    private static Field field_PacketPlayOutEntityMetadata_b; // Field to set the datawatcher changes for the packet, normally private

    private static Method method_WorldServer_b; // Method to register an entity into a world

    private static DataWatcherObject<Boolean> value_EntityCreeper_d; // DataWatcherObject that determines if a creeper is ignited, normally private
    private static Field field_EntityCreeper_fuseTicks; // Field to set the remianing fuse ticks of a creeper, normally private

    static {
        try {
            field_PacketPlayOutEntityMetadata_a = PacketPlayOutEntityMetadata.class.getDeclaredField("a");
            field_PacketPlayOutEntityMetadata_a.setAccessible(true);

            field_PacketPlayOutEntityMetadata_b = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
            field_PacketPlayOutEntityMetadata_b.setAccessible(true);

            method_WorldServer_b = WorldServer.class.getDeclaredMethod("b", Entity.class);
            method_WorldServer_b.setAccessible(true);

            Field field_EntityCreeper_d = EntityCreeper.class.getDeclaredField("c");
            field_EntityCreeper_d.setAccessible(true);
            value_EntityCreeper_d = (DataWatcherObject<Boolean>) field_EntityCreeper_d.get(null);

            field_EntityCreeper_fuseTicks = EntityCreeper.class.getDeclaredField("fuseTicks");
            field_EntityCreeper_fuseTicks.setAccessible(true);
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
            String entityType = IRegistry.ENTITY_TYPE.getKey(craftEntity.P()).toString();
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

            NBTTagList positionTagList = nbt.getList("Pos", 6);
            positionTagList.set(0, new NBTTagDouble(location.getX()));
            positionTagList.set(1, new NBTTagDouble(location.getY()));
            positionTagList.set(2, new NBTTagDouble(location.getZ()));
            nbt.set("Pos", positionTagList);
            nbt.a("UUID", UUID.randomUUID()); // Reset the UUID to resolve possible duplicates

            EntityTypes<?> entityTypes = EntityTypes.a(entityType);
            if (entityTypes != null) {
                WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

                Entity entity = entityTypes.b(
                        world,
                        nbt,
                        null,
                        null,
                        new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                        true,
                        false
                );

                if (entity == null)
                    throw new NullPointerException("Unable to create entity from NBT");

                int x = MathHelper.floor(entity.locX / 16.0D);
                int z = MathHelper.floor(entity.locZ / 16.0D);
                if (!world.isChunkLoaded(x, z, false))
                    throw new NullPointerException("Unable to spawn entity from NBT, couldn't get chunk");

                world.getChunkAt(x, z).a(entity);
                world.entityList.add(entity);
                method_WorldServer_b.invoke(world, entity);

                entity.f(nbt);

                return (LivingEntity) entity.getBukkitEntity();
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
            entity.f(nbt);
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

}
