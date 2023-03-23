package dev.rosewood.rosestacker.nms.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.entity.LivingEntity;

public abstract class AbstractSimpleStackedEntityDataStorage extends StackedEntityDataStorage {

    protected int size;

    public AbstractSimpleStackedEntityDataStorage(LivingEntity livingEntity) {
        super(StackedEntityDataStorageType.SIMPLE, livingEntity);

        this.size = 0;
    }

    public AbstractSimpleStackedEntityDataStorage(LivingEntity livingEntity, byte[] data) {
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
    public void addAll(List<EntityDataEntry> entityDataEntry) {
        this.size += entityDataEntry.size();
    }

    @Override
    public void addClones(int amount) {
        this.size += amount;
    }

    @Override
    public EntityDataEntry peek() {
        return this.copy();
    }

    @Override
    public EntityDataEntry pop() {
        if (this.isEmpty())
            throw new IllegalStateException("No more data is available");
        this.size--;
        return this.copy();
    }

    @Override
    public List<EntityDataEntry> pop(int amount) {
        amount = Math.min(amount, this.size);
        this.size -= amount;
        EntityDataEntry[] popped = new EntityDataEntry[amount];
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
    public List<EntityDataEntry> getAll() {
        EntityDataEntry[] entries = new EntityDataEntry[this.size];
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

        int amount = Math.min(count, this.size);
        LivingEntity clone = this.copy().createEntity(entity.getLocation(), false, entity.getType());
        for (int i = 0; i < amount; i++)
            consumer.accept(clone);
    }

    @Override
    public List<LivingEntity> removeIf(Function<LivingEntity, Boolean> function) {
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return List.of();

        List<LivingEntity> removedEntries = new ArrayList<>(this.size);
        LivingEntity clone = this.copy().createEntity(entity.getLocation(), false, entity.getType());
        for (int i = 0; i < this.size; i++)
            if (function.apply(clone))
                removedEntries.add(clone);

        this.size -= removedEntries.size();
        return removedEntries;
    }

    protected abstract EntityDataEntry copy();

}
