package dev.rosewood.rosestacker.nms.v1_16_R3.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataIOException;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import dev.rosewood.rosestacker.nms.v1_16_R3.NMSHandlerImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.entity.LivingEntity;

public class SimpleStackedEntityDataStorage extends StackedEntityDataStorage {

    private int size;

    public SimpleStackedEntityDataStorage(LivingEntity livingEntity) {
        super(StackedEntityDataStorageType.SIMPLE, livingEntity);

        this.size = 0;
    }

    public SimpleStackedEntityDataStorage(LivingEntity livingEntity, byte[] data) {
        this(livingEntity);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            this.size = dataInput.readInt();
        } catch (Exception e) {
            throw new StackedEntityDataIOException(e);
        }
    }

    @Override
    public void add(LivingEntity entity) {
        this.size++;
    }

    @Override
    public void addAll(List<StackedEntityDataEntry<?>> stackedEntityDataEntry) {
        this.size += stackedEntityDataEntry.size();
    }

    @Override
    public void addClones(int amount) {
        this.size += amount;
    }

    @Override
    public NBTStackedEntityDataEntry peek() {
        return this.copy();
    }

    @Override
    public NBTStackedEntityDataEntry pop() {
        if (this.isEmpty())
            throw new IllegalStateException("No more data is available");
        this.size--;
        return this.copy();
    }

    @Override
    public List<StackedEntityDataEntry<?>> pop(int amount) {
        amount = Math.min(amount, this.size);
        this.size -= amount;
        StackedEntityDataEntry<?>[] popped = new StackedEntityDataEntry[amount];
        Arrays.fill(popped, this.copy());
        return List.of(popped);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public List<StackedEntityDataEntry<?>> getAll() {
        StackedEntityDataEntry<?>[] entries = new StackedEntityDataEntry[this.size];
        Arrays.fill(entries, this.copy());
        return List.of(entries);
    }

    @Override
    public byte[] serialize(int maxAmount) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            dataOutput.writeInt(Math.min(maxAmount, this.size()));

            dataOutput.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new StackedEntityDataIOException(e);
        }
    }

    @Override
    public void forEachCapped(int count, Consumer<LivingEntity> consumer) {
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return;

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        int amount = Math.min(count, this.size);
        LivingEntity clone = nmsHandler.createEntityFromNBT(this.copy(), entity.getLocation(), false, entity.getType());
        for (int i = 0; i < amount; i++)
            consumer.accept(clone);
    }

    @Override
    public List<LivingEntity> removeIf(Function<LivingEntity, Boolean> function) {
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return List.of();

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        List<LivingEntity> removedEntries = new ArrayList<>(this.size);
        LivingEntity clone = nmsHandler.createEntityFromNBT(this.copy(), entity.getLocation(), false, entity.getType());
        for (int i = 0; i < this.size; i++)
            if (function.apply(clone))
                removedEntries.add(clone);

        this.size -= removedEntries.size();
        return removedEntries;
    }

    private NBTStackedEntityDataEntry copy() {
        NBTTagCompound tag = new NBTTagCompound();
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return new NBTStackedEntityDataEntry(tag);

        ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity, tag);
        this.stripUnneeded(tag);
        return new NBTStackedEntityDataEntry(tag);
    }

    private void stripUnneeded(NBTTagCompound compoundTag) {
        NMSHandler.REMOVABLE_NBT_KEYS.forEach(compoundTag::remove);
        NBTTagCompound bukkitValues = compoundTag.getCompound("BukkitValues");
        bukkitValues.remove("rosestacker:stacked_entity_data");
        NMSHandler.UNSAFE_NBT_KEYS.forEach(compoundTag::remove);
    }

}
