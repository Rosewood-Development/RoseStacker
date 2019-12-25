package dev.esophose.rosestacker.utils;

import dev.esophose.rosestacker.utils.reflection.craft.CraftLivingEntity;
import dev.esophose.rosestacker.utils.reflection.craft.CraftWorld;
import dev.esophose.rosestacker.utils.reflection.nms.BlockPosition;
import dev.esophose.rosestacker.utils.reflection.nms.DamageSource;
import dev.esophose.rosestacker.utils.reflection.nms.Entity;
import dev.esophose.rosestacker.utils.reflection.nms.EntityLiving;
import dev.esophose.rosestacker.utils.reflection.nms.EntityTypes;
import dev.esophose.rosestacker.utils.reflection.nms.EnumMobSpawn;
import dev.esophose.rosestacker.utils.reflection.nms.IRegistry;
import dev.esophose.rosestacker.utils.reflection.nms.NBTCompressedStreamTools;
import dev.esophose.rosestacker.utils.reflection.nms.NBTTagCompound;
import dev.esophose.rosestacker.utils.reflection.nms.NBTTagDouble;
import dev.esophose.rosestacker.utils.reflection.nms.NBTTagList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public final class EntitySerializer {

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
            EntityLiving craftEntity = new CraftLivingEntity(entity).getHandle();
            craftEntity.save(nbt);

            // Write entity type
            String entityType = IRegistry.getRegistry("ENTITY_TYPE").getKey(craftEntity.getEntityType()).toString();
            dataOutput.writeUTF(entityType);

            // Write NBT
            NBTCompressedStreamTools.compress(nbt, dataOutput);

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
     * @return the entity spawned from the NBT string
     */
    public static LivingEntity fromNBTString(String serialized, Location location) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(serialized));
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read entity type
            String entityType = dataInput.readUTF();

            // Read NBT
            NBTTagCompound nbt = NBTCompressedStreamTools.decompress(dataInput);

            Optional<EntityTypes> optionalEntity = EntityTypes.getTypeByName(entityType);
            if (optionalEntity.isPresent()) {
                Object spawned = optionalEntity.get().spawnCreature(
                        new CraftWorld(location.getWorld()).getHandle(),
                        nbt,
                        new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                        new EnumMobSpawn("TRIGGERED")
                );

                if (spawned != null) {
                    Entity entity = new Entity(spawned);
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
     * Gets a LivingEntity from an NBT string without spawning the entity into the world
     *
     * @param entityType The type of the entity to spawn
     * @param location The location that the entity would normally be spawned in
     * @param serialized The serialized entity NBT data
     * @return A LivingEntity instance, not in the world
     */
    public static LivingEntity getNBTStringAsEntity(EntityType entityType, Location location, String serialized) {
        if (location.getWorld() == null)
            return null;

        CraftWorld craftWorld = new CraftWorld(location.getWorld());
        EntityLiving entity = new EntityLiving(craftWorld.createEntity(location, entityType.getEntityClass()).getNMS());

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(serialized));
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read entity type, don't need the value
            dataInput.readUTF();

            // Read NBT
            NBTTagCompound nbt = NBTCompressedStreamTools.decompress(dataInput);

            // Set NBT
            setNBT(entity, nbt, location);

            // Update loot table
            entity.updateLootTable(new DamageSource("GENERIC"), false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (LivingEntity) entity.getBukkitEntity();
    }

    /**
     * Creates a LivingEntity instance where the actual entity has not been added to the world
     *
     * @param entityType The type of the entity to spawn
     * @param location The location the entity would have spawned at
     * @return The newly created LivingEntity instance
     */
    public static LivingEntity createEntityUnspawned(EntityType entityType, Location location) {
        if (location.getWorld() == null)
            return null;

        CraftWorld craftWorld = new CraftWorld(location.getWorld());
        return (LivingEntity) craftWorld.createEntity(location, entityType.getEntityClass()).getBukkitEntity();
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
        entity.setNBT(nbt);
    }

    /**
     * A method to serialize an NBT String list to a byte array.
     *
     * @param nbtStrings to turn into a byte array.
     * @return byte array of the items.
     */
    public static byte[] toBlob(List<String> nbtStrings) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            // Write the size of the items
            dataOutput.writeInt(nbtStrings.size());

            // Save every element in the list
            for (String nbtString : nbtStrings)
                dataOutput.writeUTF(nbtString);

            // Serialize that array
            dataOutput.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets a list of NBT Strings from a byte array.
     *
     * @param data byte array to convert to NBT String list.
     * @return NBT String list created from the byte array.
     */
    public static List<String> fromBlob(byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
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
