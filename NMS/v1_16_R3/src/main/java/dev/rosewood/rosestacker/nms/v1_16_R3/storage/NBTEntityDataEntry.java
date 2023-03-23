package dev.rosewood.rosestacker.nms.v1_16_R3.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
import dev.rosewood.rosestacker.nms.v1_16_R3.NMSHandlerImpl;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkStatus;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.IChunkAccess;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagDouble;
import net.minecraft.server.v1_16_R3.NBTTagFloat;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class NBTEntityDataEntry implements EntityDataEntry {

    private final NBTTagCompound compoundTag;

    public NBTEntityDataEntry(LivingEntity livingEntity) {
        this.compoundTag = new NBTTagCompound();
        ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(livingEntity, this.compoundTag);
    }

    public NBTEntityDataEntry(NBTTagCompound compoundTag) {
        this.compoundTag = compoundTag;
    }

    public NBTTagCompound get() {
        return this.compoundTag;
    }

    @Override
    public LivingEntity createEntity(Location location, boolean addToWorld, EntityType entityType) {
        try {
            NMSHandlerImpl nmsHandler = (NMSHandlerImpl) NMSAdapter.getHandler();
            NBTTagCompound nbt = this.compoundTag.clone();

            NBTTagList positionTagList = nbt.getList("Pos", 6);
            if (positionTagList == null)
                positionTagList = new NBTTagList();
            this.setTag(positionTagList, 0, NBTTagDouble.a(location.getX()));
            this.setTag(positionTagList, 1, NBTTagDouble.a(location.getY()));
            this.setTag(positionTagList, 2, NBTTagDouble.a(location.getZ()));
            nbt.set("Pos", positionTagList);
            NBTTagList rotationTagList = nbt.getList("Rotation", 5);
            if (rotationTagList == null)
                rotationTagList = new NBTTagList();
            this.setTag(rotationTagList, 0, NBTTagFloat.a(location.getYaw()));
            this.setTag(rotationTagList, 1, NBTTagFloat.a(location.getPitch()));
            nbt.set("Rotation", rotationTagList);
            nbt.a("UUID", UUID.randomUUID()); // Reset the UUID to resolve possible duplicates

            Optional<EntityTypes<?>> optionalEntity = EntityTypes.a(entityType.getKey().getKey());
            if (optionalEntity.isPresent()) {
                WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

                Entity entity = nmsHandler.createCreature(
                        optionalEntity.get(),
                        world,
                        nbt,
                        null,
                        null,
                        new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                        EnumMobSpawn.COMMAND
                );

                if (entity == null)
                    throw new NullPointerException("Unable to create entity from NBT");

                // Load NBT
                entity.load(nbt);

                if (addToWorld) {
                    IChunkAccess ichunkaccess = world.getChunkAt(MathHelper.floor(entity.locX() / 16.0D), MathHelper.floor(entity.locZ() / 16.0D), ChunkStatus.FULL, true);
                    if (!(ichunkaccess instanceof Chunk))
                        throw new NullPointerException("Unable to spawn entity from NBT, couldn't get chunk");

                    ichunkaccess.a(entity);
                    nmsHandler.registerEntity(world, entity);
                    entity.noDamageTicks = 0;
                }

                return (LivingEntity) entity.getBukkitEntity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setTag(NBTTagList tag, int index, NBTBase value) {
        if (index >= tag.size()) {
            tag.b(index, value);
        } else {
            tag.a(index, value);
        }
    }

}
