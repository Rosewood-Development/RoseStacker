package dev.esophose.rosestacker.nms.v1_13_R2;

import dev.esophose.rosestacker.nms.NMSHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagDouble;
import net.minecraft.server.v1_13_R2.NBTTagList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class NMSHandlerImpl implements NMSHandler {

    @Override
    public String getEntityAsNBTString(LivingEntity livingEntity) {
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
        nbtTagList.set(0, new NBTTagDouble(desiredLocation.getX()));
        nbtTagList.set(1, new NBTTagDouble(desiredLocation.getY()));
        nbtTagList.set(2, new NBTTagDouble(desiredLocation.getZ()));
        nbt.set("Pos", nbtTagList);
        entity.f(nbt);
    }

}
