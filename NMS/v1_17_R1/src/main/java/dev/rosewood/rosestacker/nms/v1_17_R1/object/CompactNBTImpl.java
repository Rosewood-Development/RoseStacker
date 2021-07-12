package dev.rosewood.rosestacker.nms.v1_17_R1.object;

import dev.rosewood.rosestacker.nms.object.CompactNBT;
import dev.rosewood.rosestacker.nms.object.CompactNBTException;
import dev.rosewood.rosestacker.nms.object.WrappedNBT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class CompactNBTImpl implements CompactNBT {

    private final CompoundTag base;
    private final List<CompoundTag> data;

    public CompactNBTImpl(LivingEntity livingEntity) {
        this.base = new CompoundTag();
        ((CraftLivingEntity) livingEntity).getHandle().saveWithoutId(this.base);
        this.stripUnneeded(this.base);
        this.stripAttributeUuids(this.base);

        this.data = Collections.synchronizedList(new LinkedList<>());
    }

    public CompactNBTImpl(byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            this.base = NbtIo.read(dataInput);
            int length = dataInput.readInt();
            List<CompoundTag> tags = new LinkedList<>();
            for (int i = 0; i < length; i++)
                tags.add(NbtIo.read(dataInput));
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
        synchronized (this.data) {
            List<WrappedNBT<?>> wrapped = new ArrayList<>(this.data.size());
            for (CompoundTag compoundTag : this.data)
                wrapped.add(new WrappedNBTImpl(this.rebuild(compoundTag)));
            return wrapped;
        }
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            NbtIo.write(this.base, dataOutput);
            synchronized (this.data) {
                dataOutput.writeInt(this.data.size());
                for (CompoundTag compoundTag : this.data)
                    NbtIo.write(compoundTag, dataOutput);
            }

            dataOutput.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new CompactNBTException(e);
        }
    }

    private void addAt(int index, LivingEntity livingEntity) {
        CompoundTag compoundTag = new CompoundTag();
        ((CraftLivingEntity) livingEntity).getHandle().saveWithoutId(compoundTag);
        this.stripUnneeded(compoundTag);
        this.stripAttributeUuids(compoundTag);
        this.removeDuplicates(compoundTag);
        this.data.add(index, compoundTag);
    }

    private void addAt(int index, WrappedNBT<?> wrappedNBT) {
        CompoundTag compoundTag = (CompoundTag) wrappedNBT.get();
        this.stripUnneeded(compoundTag);
        this.stripAttributeUuids(compoundTag);
        this.removeDuplicates(compoundTag);
        this.data.add(index, compoundTag);
    }

    private void removeDuplicates(CompoundTag compoundTag) {
        for (String key : new ArrayList<>(compoundTag.getAllKeys())) {
            Tag baseValue = this.base.get(key);
            Tag thisValue = compoundTag.get(key);
            if (baseValue != null && baseValue.equals(thisValue))
                compoundTag.remove(key);
        }
    }

    private CompoundTag rebuild(CompoundTag compoundTag) {
        CompoundTag merged = new CompoundTag();
        merged.merge(this.base);
        merged.merge(compoundTag);
        this.fillAttributeUuids(merged);
        return merged;
    }

    private void stripUnneeded(CompoundTag compoundTag) {
        compoundTag.remove("UUID");
        compoundTag.remove("Pos");
        compoundTag.remove("Rotation");
        compoundTag.remove("WorldUUIDMost");
        compoundTag.remove("WorldUUIDLeast");
        compoundTag.remove("Motion");
        compoundTag.remove("OnGround");
        compoundTag.remove("FallDistance");
        compoundTag.remove("Spigot.ticksLived");
        compoundTag.remove("Paper.OriginWorld");
        compoundTag.remove("Paper.Origin");
        CompoundTag bukkitValues = compoundTag.getCompound("BukkitValues");
        bukkitValues.remove("rosestacker:stacked_entity_data");
    }

    private void stripAttributeUuids(CompoundTag compoundTag) {
        ListTag attributes = compoundTag.getList("Attributes", Tag.TAG_COMPOUND);
        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attribute = attributes.getCompound(i);
            attribute.remove("UUID");
            ListTag modifiers = attribute.getList("Modifiers", Tag.TAG_COMPOUND);
            for (int j = 0; j < modifiers.size(); j++) {
                CompoundTag modifier = modifiers.getCompound(j);
                if (modifier.getString("Name").equals("Random spawn bonus")) {
                    modifiers.remove(j);
                    j--;
                } else {
                    modifier.remove("UUID");
                }
            }
        }
    }

    private void fillAttributeUuids(CompoundTag compoundTag) {
        ListTag attributes = compoundTag.getList("Attributes", Tag.TAG_COMPOUND);
        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attribute = attributes.getCompound(i);
            attribute.putUUID("UUID", UUID.randomUUID());
            ListTag modifiers = attribute.getList("Modifiers", Tag.TAG_COMPOUND);
            for (int j = 0; j < modifiers.size(); j++) {
                CompoundTag modifier = modifiers.getCompound(j);
                modifier.putUUID("UUID", UUID.randomUUID());
            }
            if (modifiers.size() == 0)
                attribute.remove("Modifiers");
        }
    }

}
