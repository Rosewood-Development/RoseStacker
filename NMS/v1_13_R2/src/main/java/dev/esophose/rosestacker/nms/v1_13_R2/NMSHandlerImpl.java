package dev.esophose.rosestacker.nms.v1_13_R2;

import dev.esophose.rosestacker.nms.NMSHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.DataWatcher;
import net.minecraft.server.v1_13_R2.DataWatcherRegistry;
import net.minecraft.server.v1_13_R2.DataWatcherSerializer;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagDouble;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityMetadata;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class NMSHandlerImpl implements NMSHandler {

    private static Field field_PacketPlayOutEntityMetadata_a; // Field to set the entity ID for the packet, normally private
    private static Field field_PacketPlayOutEntityMetadata_b; // Field to set the datawatcher changes for the packet, normally private

    static {
        try {
            field_PacketPlayOutEntityMetadata_a = PacketPlayOutEntityMetadata.class.getDeclaredField("a");
            field_PacketPlayOutEntityMetadata_a.setAccessible(true);

            field_PacketPlayOutEntityMetadata_b = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
            field_PacketPlayOutEntityMetadata_b.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getEntityAsNBT(LivingEntity livingEntity) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            NBTTagCompound nbt = new NBTTagCompound();
            EntityLiving craftEntity = ((CraftLivingEntity) livingEntity).getHandle();
            craftEntity.save(nbt);

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

            EntityTypes<?> entityTypes = EntityTypes.a(entityType);
            if (entityTypes != null) {
                Entity spawned = entityTypes.spawnCreature(
                        ((CraftWorld) location.getWorld()).getHandle(),
                        nbt,
                        null,
                        null,
                        new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                        true,
                        false,
                        SpawnReason.CUSTOM
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
            field_PacketPlayOutEntityMetadata_b.set(packetPlayOutEntityMetadata, Collections.singletonList(dataWatcherItem));

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutEntityMetadata);
        } catch (Exception e) {
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
        nbtTagList.set(0, new NBTTagDouble(desiredLocation.getX()));
        nbtTagList.set(1, new NBTTagDouble(desiredLocation.getY()));
        nbtTagList.set(2, new NBTTagDouble(desiredLocation.getZ()));
        nbt.set("Pos", nbtTagList);
        entity.f(nbt);
    }

}
