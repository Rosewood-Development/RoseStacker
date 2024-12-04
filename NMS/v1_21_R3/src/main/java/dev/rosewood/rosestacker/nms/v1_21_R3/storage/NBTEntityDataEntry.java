package dev.rosewood.rosestacker.nms.v1_21_R3.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
import dev.rosewood.rosestacker.nms.v1_21_R3.NMSHandlerImpl;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class NBTEntityDataEntry implements EntityDataEntry {

    private final CompoundTag compoundTag;

    public NBTEntityDataEntry(LivingEntity livingEntity) {
        this.compoundTag = new CompoundTag();
        ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(livingEntity, this.compoundTag);
    }

    public NBTEntityDataEntry(CompoundTag compoundTag) {
        this.compoundTag = compoundTag;
    }

    public CompoundTag get() {
        return this.compoundTag;
    }

    @Override
    public LivingEntity createEntity(Location location, boolean addToWorld, EntityType entityType) {
        try {
            NMSHandlerImpl nmsHandler = (NMSHandlerImpl) NMSAdapter.getHandler();
            CompoundTag nbt = this.compoundTag.copy();

            ListTag positionTagList = nbt.getList("Pos", Tag.TAG_DOUBLE);
            if (positionTagList == null)
                positionTagList = new ListTag();
            this.setTag(positionTagList, 0, DoubleTag.valueOf(location.getX()));
            this.setTag(positionTagList, 1, DoubleTag.valueOf(location.getY()));
            this.setTag(positionTagList, 2, DoubleTag.valueOf(location.getZ()));
            nbt.put("Pos", positionTagList);
            ListTag rotationTagList = nbt.getList("Rotation", Tag.TAG_FLOAT);
            if (rotationTagList == null)
                rotationTagList = new ListTag();
            this.setTag(rotationTagList, 0, FloatTag.valueOf(location.getYaw()));
            this.setTag(rotationTagList, 1, FloatTag.valueOf(location.getPitch()));
            nbt.put("Rotation", rotationTagList);
            nbt.putUUID("UUID", UUID.randomUUID()); // Reset the UUID to resolve possible duplicates

            Optional<net.minecraft.world.entity.EntityType<?>> optionalEntity = net.minecraft.world.entity.EntityType.byString(entityType.getKey().getKey());
            if (optionalEntity.isPresent()) {
                ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();

                Entity entity = nmsHandler.createCreature(
                        optionalEntity.get(),
                        world,
                        new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                        EntitySpawnReason.COMMAND
                );

                if (entity == null)
                    throw new NullPointerException("Unable to create entity from NBT");

                // Load NBT
                entity.load(nbt);

                if (addToWorld) {
                    nmsHandler.addEntityToWorld(world, entity);
                    entity.invulnerableTime = 0;
                }

                return (LivingEntity) entity.getBukkitEntity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setTag(ListTag tag, int index, Tag value) {
        if (index >= tag.size()) {
            tag.addTag(index, value);
        } else {
            tag.setTag(index, value);
        }
    }

}
