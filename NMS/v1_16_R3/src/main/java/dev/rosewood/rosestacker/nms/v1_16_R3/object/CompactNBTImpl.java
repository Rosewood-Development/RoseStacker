package dev.rosewood.rosestacker.nms.v1_16_R3.object;

import dev.rosewood.rosestacker.nms.object.CompactNBT;
import dev.rosewood.rosestacker.nms.object.CompactNBTException;
import dev.rosewood.rosestacker.nms.object.WrappedNBT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class CompactNBTImpl implements CompactNBT {

    private final NBTTagCompound base;
    private final List<NBTTagCompound> data;

    public CompactNBTImpl(LivingEntity livingEntity) {
        this.base = new NBTTagCompound();
        ((CraftLivingEntity) livingEntity).getHandle().save(this.base);
        this.stripUnneeded(this.base);
        this.stripAttributeUuids(this.base);

        this.data = Collections.synchronizedList(new LinkedList<>());
    }

    public CompactNBTImpl(byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            this.base = NBTCompressedStreamTools.a((DataInput) dataInput);
            int length = dataInput.readInt();
            List<NBTTagCompound> tags = new LinkedList<>();
            for (int i = 0; i < length; i++)
                tags.add(NBTCompressedStreamTools.a((DataInput) dataInput));
            this.data = Collections.synchronizedList(tags);
        } catch (Exception e) {
            throw new CompactNBTException(e);
        }
    }

    @Override
    public void addFirst(LivingEntity entity) {
        this.addAt(0, entity);
    }

    @Override
    public void addLast(LivingEntity entity) {
        this.addAt(this.data.size(), entity);
    }

    @Override
    public void addAllFirst(List<WrappedNBT<?>> wrappedNbt) {
        wrappedNbt.forEach(x -> this.addAt(0, x));
    }

    @Override
    public void addAllLast(List<WrappedNBT<?>> wrappedNbt) {
        wrappedNbt.forEach(x -> this.addAt(this.data.size(), x));
    }

    @Override
    public WrappedNBTImpl peek() {
        return new WrappedNBTImpl(this.rebuild(this.data.get(0)));
    }

    @Override
    public WrappedNBTImpl pop() {
        return new WrappedNBTImpl(this.rebuild(this.data.remove(0)));
    }

    @Override
    public int size() {
        return this.data.size();
    }

    @Override
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @Override
    public List<WrappedNBT<?>> getAll() {
        List<WrappedNBT<?>> wrapped = new ArrayList<>(this.data.size());
        for (NBTTagCompound compoundTag : new ArrayList<>(this.data))
            wrapped.add(new WrappedNBTImpl(this.rebuild(compoundTag)));
        return wrapped;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            NBTCompressedStreamTools.a(this.base, (DataOutput) dataOutput);
            dataOutput.writeInt(this.data.size());
            for (NBTTagCompound compoundTag : new ArrayList<>(this.data))
                NBTCompressedStreamTools.a(compoundTag, (DataOutput) dataOutput);

            dataOutput.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new CompactNBTException(e);
        }
    }

    private void addAt(int index, LivingEntity livingEntity) {
        NBTTagCompound compoundTag = new NBTTagCompound();
        ((CraftLivingEntity) livingEntity).getHandle().save(compoundTag);
        this.stripUnneeded(compoundTag);
        this.stripAttributeUuids(compoundTag);
        this.removeDuplicates(compoundTag);
        this.data.add(index, compoundTag);
    }

    private void addAt(int index, WrappedNBT<?> wrappedNBT) {
        NBTTagCompound compoundTag = (NBTTagCompound) wrappedNBT.get();
        this.stripUnneeded(compoundTag);
        this.stripAttributeUuids(compoundTag);
        this.removeDuplicates(compoundTag);
        this.data.add(index, compoundTag);
    }

    private void removeDuplicates(NBTTagCompound compoundTag) {
        for (String key : new ArrayList<>(compoundTag.getKeys())) {
            NBTBase baseValue = this.base.get(key);
            NBTBase thisValue = compoundTag.get(key);
            if (baseValue != null && baseValue.equals(thisValue))
                compoundTag.remove(key);
        }
    }

    private NBTTagCompound rebuild(NBTTagCompound compoundTag) {
        NBTTagCompound merged = new NBTTagCompound();
        merged.a(this.base);
        merged.a(compoundTag);
        this.fillAttributeUuids(merged);
        return merged;
    }

    private void stripUnneeded(NBTTagCompound compoundTag) {
        compoundTag.remove("UUID");
        compoundTag.remove("Pos");
        compoundTag.remove("Rotation");
        compoundTag.remove("WorldUUIDMost");
        compoundTag.remove("WorldUUIDLeast");
        compoundTag.remove("Motion");
        compoundTag.remove("OnGround");
        compoundTag.remove("FallDistance");
        compoundTag.remove("Leash");
        compoundTag.remove("Spigot.ticksLived");
        compoundTag.remove("Paper.OriginWorld");
        compoundTag.remove("Paper.Origin");
        NBTTagCompound bukkitValues = compoundTag.getCompound("BukkitValues");
        bukkitValues.remove("rosestacker:stacked_entity_data");
    }

    private void stripAttributeUuids(NBTTagCompound compoundTag) {
        NBTTagList attributes = compoundTag.getList("Attributes", 10);
        for (int i = 0; i < attributes.size(); i++) {
            NBTTagCompound attribute = attributes.getCompound(i);
            attribute.remove("UUID");
            NBTTagList modifiers = attribute.getList("Modifiers", 10);
            for (int j = 0; j < modifiers.size(); j++) {
                NBTTagCompound modifier = modifiers.getCompound(j);
                if (modifier.getString("Name").equals("Random spawn bonus")) {
                    modifiers.remove(j);
                    j--;
                } else {
                    modifier.remove("UUID");
                }
            }
        }
    }

    private void fillAttributeUuids(NBTTagCompound compoundTag) {
        NBTTagList attributes = compoundTag.getList("Attributes", 10);
        for (int i = 0; i < attributes.size(); i++) {
            NBTTagCompound attribute = attributes.getCompound(i);
            attribute.a("UUID", UUID.randomUUID());
            NBTTagList modifiers = attribute.getList("Modifiers", 10);
            for (int j = 0; j < modifiers.size(); j++) {
                NBTTagCompound modifier = modifiers.getCompound(j);
                modifier.a("UUID", UUID.randomUUID());
            }
            if (modifiers.size() == 0)
                attribute.remove("Modifiers");
        }
    }

}
