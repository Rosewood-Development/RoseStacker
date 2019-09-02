package dev.esophose.rosestacker.utils;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.DamageSource;
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
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class EntitySerializer {

    private static Method entityLiving_a;

    static {
        try {
            entityLiving_a = EntityLiving.class.getDeclaredMethod("a", DamageSource.class, boolean.class);
            entityLiving_a.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Serializes a LivingEntity to a base64 string
     *
     * @param entity to serialize
     * @return base64 string of the entity
     */
    public static String toNBTString(LivingEntity entity) {
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
    public static LivingEntity fromNBTString(String serialized, Location location) {
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
                    setNBT(entity, nbt, location);
                    return (LivingEntity) entity.getBukkitEntity();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Applies a serialized NBT string to an existing entity
     *
     * @param targetEntity The entity to apply properties to
     * @param serialized The serialized entity NBT data
     */
    public static void applyNBTStringToEntity(LivingEntity targetEntity, String serialized) {
        EntityLiving entity = (EntityLiving) ((CraftEntity) targetEntity).getHandle();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(serialized));
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read entity type, don't need the value
            dataInput.readUTF();

            // Read NBT
            NBTTagCompound nbt = NBTCompressedStreamTools.a(dataInput);

            // Set NBT
            setNBT(entity, nbt, targetEntity.getLocation());

            // Update loot table
            entityLiving_a.invoke(entity, DamageSource.GENERIC, false);
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
    private static void setNBT(Entity entity, NBTTagCompound nbt, Location desiredLocation) {
        NBTTagList nbtTagList = nbt.getList("Pos", 6);
        nbtTagList.set(0, new NBTTagDouble(desiredLocation.getX()));
        nbtTagList.set(1, new NBTTagDouble(desiredLocation.getY()));
        nbtTagList.set(2, new NBTTagDouble(desiredLocation.getZ()));
        nbt.set("Pos", nbtTagList);
        entity.f(nbt);
    }

    /**
     * A method to serialize an NBT String list to Base64 String.
     *
     * @param nbtStrings to turn into a Base64 String.
     * @return Base64 string of the items.
     */
    public static String toBase64(List<String> nbtStrings) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            // Write the size of the items
            dataOutput.writeInt(nbtStrings.size());

            // Save every element in the list
            for (String nbtString : nbtStrings)
                dataOutput.writeUTF(nbtString);

            // Serialize that array
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets a list of NBT Strings from Base64 string.
     *
     * @param data Base64 string to convert to NBT String list.
     * @return NBT String list created from the Base64 string.
     */
    public static List<String> fromBase64(String data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            int length = dataInput.readInt();
            List<String> items = new LinkedList<>();

            // Read the serialized itemstack list
            for (int i = 0; i < length; i++)
                items.add(dataInput.readUTF());

            return items;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
