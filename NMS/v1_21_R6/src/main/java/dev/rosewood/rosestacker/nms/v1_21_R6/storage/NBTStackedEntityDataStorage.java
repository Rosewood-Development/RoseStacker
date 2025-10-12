package dev.rosewood.rosestacker.nms.v1_21_R6.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataIOException;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import dev.rosewood.rosestacker.nms.storage.StorageMigrationType;
import dev.rosewood.rosestacker.nms.v1_21_R6.NMSHandlerImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.LivingEntity;

public class NBTStackedEntityDataStorage extends StackedEntityDataStorage {

    private final CompoundTag base;
    private final Queue<CompoundTag> data;

    public NBTStackedEntityDataStorage(LivingEntity livingEntity) {
        super(StackedEntityDataStorageType.NBT, livingEntity);

        this.base = ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(livingEntity);
        this.stripUnneeded(this.base);
        this.stripAttributeUuids(this.base);

        this.data = createBackingQueue();
    }

    public NBTStackedEntityDataStorage(LivingEntity livingEntity, byte[] data, Set<StorageMigrationType> migrations) {
        super(StackedEntityDataStorageType.NBT, livingEntity);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            this.base = this.migrate(migrations, NbtIo.read(dataInput));
            int length = dataInput.readInt();
            this.data = createBackingQueue();
            for (int i = 0; i < length; i++)
                this.data.add(this.migrate(migrations, NbtIo.read(dataInput)));
        } catch (Exception e) {
            throw new StackedEntityDataIOException(e);
        }
    }

    private CompoundTag migrate(Set<StorageMigrationType> migrations, CompoundTag tag) {
        if (migrations.isEmpty())
            return tag;

        if (migrations.contains(StorageMigrationType.STRIP_OLD_ATTRIBUTES))
            tag.remove("attributes");

        return tag;
    }

    @Override
    public void add(LivingEntity entity) {
        CompoundTag compoundTag = ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity);
        this.stripUnneeded(compoundTag);
        this.stripAttributeUuids(compoundTag);
        this.removeDuplicates(compoundTag);
        this.data.add(compoundTag);
    }

    @Override
    public void addAll(StackedEntityDataStorage stackedEntityDataStorage) {
        stackedEntityDataStorage.getAll().forEach(entry -> {
            CompoundTag compoundTag = ((NBTEntityDataEntry) entry).get();
            this.stripUnneeded(compoundTag);
            this.stripAttributeUuids(compoundTag);
            this.removeDuplicates(compoundTag);
            this.data.add(compoundTag);
        });
    }

    @Override
    public void addClones(int amount) {
        for (int i = 0; i < amount; i++)
            this.data.add(this.base.copy());
    }

    @Override
    public NBTEntityDataEntry peek() {
        return new NBTEntityDataEntry(this.rebuild(this.data.element()));
    }

    @Override
    public NBTEntityDataEntry pop() {
        return new NBTEntityDataEntry(this.rebuild(this.data.remove()));
    }

    @Override
    public List<EntityDataEntry> pop(int amount) {
        amount = Math.min(amount, this.data.size());

        List<EntityDataEntry> popped = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++)
            popped.add(new NBTEntityDataEntry(this.rebuild(this.data.remove())));
        return popped;
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
    public List<EntityDataEntry> getAll() {
        List<EntityDataEntry> wrapped = new ArrayList<>(this.data.size());
        for (CompoundTag compoundTag : new ArrayList<>(this.data))
            wrapped.add(new NBTEntityDataEntry(this.rebuild(compoundTag)));
        return wrapped;
    }

    @Override
    public byte[] serialize(int maxAmount) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            int targetAmount = Math.min(maxAmount, this.data.size());
            List<CompoundTag> tagsToSave = new ArrayList<>(targetAmount);
            Iterator<CompoundTag> iterator = this.data.iterator();
            for (int i = 0; i < targetAmount; i++)
                tagsToSave.add(iterator.next());

            NbtIo.write(this.base, dataOutput);
            dataOutput.writeInt(tagsToSave.size());
            for (CompoundTag compoundTag : tagsToSave)
                NbtIo.write(compoundTag, dataOutput);

            dataOutput.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new StackedEntityDataIOException(e);
        }
    }

    @Override
    public void forEach(Consumer<LivingEntity> consumer) {
        this.forEachCapped(Integer.MAX_VALUE, consumer);
    }

    @Override
    public void forEachCapped(int count, Consumer<LivingEntity> consumer) {
        if (count > this.data.size())
            count = this.data.size();

        LivingEntity thisEntity = this.entity.get();
        if (thisEntity == null)
            return;

        Iterator<CompoundTag> iterator = this.data.iterator();
        for (int i = 0; i < count; i++) {
            CompoundTag compoundTag = iterator.next();
            LivingEntity entity = new NBTEntityDataEntry(this.rebuild(compoundTag)).createEntity(thisEntity.getLocation(), false, thisEntity.getType());
            consumer.accept(entity);
        }
    }

    @Override
    public void forEachTransforming(Function<LivingEntity, Boolean> function) {
        LivingEntity thisEntity = this.entity.get();
        if (thisEntity == null)
            return;

        synchronized (this.data) {
            List<CompoundTag> data = new ArrayList<>(this.data);
            ListIterator<CompoundTag> dataIterator = data.listIterator();
            while (dataIterator.hasNext()) {
                CompoundTag compoundTag = dataIterator.next();
                LivingEntity entity = new NBTEntityDataEntry(this.rebuild(compoundTag)).createEntity(thisEntity.getLocation(), false, thisEntity.getType());
                if (function.apply(entity)) {
                    CompoundTag replacementTag = ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity);
                    this.stripUnneeded(replacementTag);
                    this.stripAttributeUuids(replacementTag);
                    this.removeDuplicates(replacementTag);
                    dataIterator.set(replacementTag);
                }
            }

            this.data.clear();
            this.data.addAll(data);
        }
    }

    @Override
    public List<LivingEntity> removeIf(Function<LivingEntity, Boolean> function) {
        List<LivingEntity> removedEntries = new ArrayList<>(this.data.size());
        LivingEntity thisEntity = this.entity.get();
        if (thisEntity == null)
            return removedEntries;

        synchronized (this.data) {
            List<CompoundTag> data = new ArrayList<>(this.data);
            ListIterator<CompoundTag> dataIterator = data.listIterator();
            while (dataIterator.hasNext()) {
                CompoundTag compoundTag = dataIterator.next();
                LivingEntity entity = new NBTEntityDataEntry(this.rebuild(compoundTag)).createEntity(thisEntity.getLocation(), false, thisEntity.getType());
                if (function.apply(entity)) {
                    removedEntries.add(entity);
                    dataIterator.remove();
                } else {
                    CompoundTag replacementTag = ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity);
                    this.stripUnneeded(replacementTag);
                    this.stripAttributeUuids(replacementTag);
                    this.removeDuplicates(replacementTag);
                    dataIterator.set(replacementTag);
                }
            }

            this.data.clear();
            this.data.addAll(data);
            return removedEntries;
        }
    }

    private void removeDuplicates(CompoundTag compoundTag) {
        for (String key : new ArrayList<>(compoundTag.keySet())) {
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
        NMSHandler.REMOVABLE_NBT_KEYS.forEach(compoundTag::remove);
        CompoundTag bukkitValues = compoundTag.getCompoundOrEmpty("BukkitValues");
        bukkitValues.remove("rosestacker:stacked_entity_data");
    }

    private void stripAttributeUuids(CompoundTag compoundTag) {
        ListTag attributes = compoundTag.getListOrEmpty("Attributes");
        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attribute = attributes.getCompoundOrEmpty(i);
            attribute.remove("UUID");
            ListTag modifiers = attribute.getListOrEmpty("Modifiers");
            for (int j = 0; j < modifiers.size(); j++) {
                CompoundTag modifier = modifiers.getCompoundOrEmpty(j);
                if (modifier.getStringOr("Name", "").equals("Random spawn bonus")) {
                    modifiers.remove(j);
                    j--;
                } else {
                    modifier.remove("UUID");
                }
            }
        }
    }

    private void fillAttributeUuids(CompoundTag compoundTag) {
        ListTag attributes = compoundTag.getListOrEmpty("Attributes");
        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attribute = attributes.getCompoundOrEmpty(i);
            attribute.store("UUID", UUIDUtil.CODEC, UUID.randomUUID());
            ListTag modifiers = attribute.getListOrEmpty("Modifiers");
            for (int j = 0; j < modifiers.size(); j++) {
                CompoundTag modifier = modifiers.getCompoundOrEmpty(j);
                modifier.store("UUID", UUIDUtil.CODEC, UUID.randomUUID());
            }
            if (modifiers.isEmpty())
                attribute.remove("Modifiers");
        }
    }

}
