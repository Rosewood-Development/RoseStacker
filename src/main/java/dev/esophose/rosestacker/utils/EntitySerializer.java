package dev.esophose.rosestacker.utils;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumMobSpawn;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.NBTTagDouble;
import net.minecraft.server.v1_14_R1.NBTTagList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Optional;

public class EntitySerializer {

    /**
     * Serializes a LivingEntity to a base64 string
     *
     * @param entity to serialize
     * @return base64 string of the entity
     */
    public static String serialize(LivingEntity entity) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            NBTTagCompound nbt = new NBTTagCompound();
            EntityLiving craftEntity = ((CraftLivingEntity) entity).getHandle();
            craftEntity.save(nbt);

            // Write entity type
            String entityType = IRegistry.ENTITY_TYPE.getKey(craftEntity.getEntityType()).toString();
            dataOutput.writeUTF(entityType);

            // Write NBT
            NBTCompressedStreamTools.a(nbt, (OutputStream) dataOutput);

            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Deserializes and spawns the entity at the given location
     *
     * @param serialized entity
     * @param location to spawn the entity at
     */
    public static LivingEntity deserialize(String serialized, Location location) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(serialized));
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read entity type
            String entityType = dataInput.readUTF();

            // Read NBT
            NBTTagCompound nbt = NBTCompressedStreamTools.a(dataInput);

            Optional<EntityTypes<?>> optionalEntity = EntityTypes.a(entityType);
            if (optionalEntity.isPresent()) {
                Entity entity = optionalEntity.get().spawnCreature(
                        ((CraftWorld) location.getWorld()).getHandle(),
                        nbt,
                        null,
                        null,
                        new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                        EnumMobSpawn.TRIGGERED,
                        true,
                        false
                );

                if (entity != null) {
                    NBTTagList nbtTagList = nbt.getList("Pos", 6);
                    nbtTagList.set(0, new NBTTagDouble(location.getX()));
                    nbtTagList.set(1, new NBTTagDouble(location.getY()));
                    nbtTagList.set(2, new NBTTagDouble(location.getZ()));
                    nbt.set("Pos", nbtTagList);

                    entity.f(nbt);
                    return (LivingEntity) entity.getBukkitEntity();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
