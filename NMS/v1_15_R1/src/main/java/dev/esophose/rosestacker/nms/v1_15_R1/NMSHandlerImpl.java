package dev.esophose.rosestacker.nms.v1_15_R1;

import dev.esophose.rosestacker.nms.NMSHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagDouble;
import net.minecraft.server.v1_15_R1.NBTTagList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class NMSHandlerImpl implements NMSHandler {

    // Method to update the EntityLiving LootTable, normally protected
    private static Method method_EntityLiving_a;

    static {
        try {
            method_EntityLiving_a = EntityLiving.class.getDeclaredMethod("a", DamageSource.class, boolean.class);
            method_EntityLiving_a.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getEntityAsNBTString(LivingEntity livingEntity) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            NBTTagCompound nbt = new NBTTagCompound();
            EntityLiving craftEntity = ((CraftLivingEntity) livingEntity).getHandle();
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

    @Override
    public LivingEntity spawnEntityFromNBTString(String serialized, Location location) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(serialized));
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
                        EnumMobSpawn.TRIGGERED,
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
    public LivingEntity getNBTStringAsEntity(EntityType entityType, Location location, String serialized) {
        if (location.getWorld() == null)
            return null;

        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        EntityLiving entity = (EntityLiving) craftWorld.createEntity(location, entityType.getEntityClass());

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(serialized));
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
