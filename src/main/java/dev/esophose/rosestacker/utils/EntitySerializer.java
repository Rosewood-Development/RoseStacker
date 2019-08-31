package dev.esophose.rosestacker.utils;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumMobSpawn;
import net.minecraft.server.v1_14_R1.NBTReadLimiter;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

public class EntitySerializer {

    public static String serialize(LivingEntity entity) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            NBTTagCompound nbt = new NBTTagCompound();
            EntityLiving craftEntity = ((CraftLivingEntity) entity).getHandle();
            craftEntity.save(nbt);

            // Write entity type
            String entityType = craftEntity.cG().toString();
            dataOutput.writeUTF(entityType);

            // Write NBT
            nbt.write(dataOutput);

            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Unserializes and spawns the entity at the given location
     *
     * @param serialized entity
     * @param location to spawn the entity at
     */
    public static void unserialize(String serialized, Location location) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(serialized));
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read entity type
            String entityType = dataInput.readUTF();

            // Read NBT
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.load(dataInput, 512, NBTReadLimiter.a);

            Optional<EntityTypes<?>> optionalEntity = EntityTypes.a(entityType);
            optionalEntity.ifPresent(entityTypes -> entityTypes.spawnCreature(
                    ((CraftWorld) location.getWorld()).getHandle(),
                    nbt,
                    null,
                    null,
                    new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                    EnumMobSpawn.TRIGGERED,
                    true,
                    false
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
