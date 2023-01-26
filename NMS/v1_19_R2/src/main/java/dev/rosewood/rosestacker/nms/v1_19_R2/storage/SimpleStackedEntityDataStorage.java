package dev.rosewood.rosestacker.nms.v1_19_R2.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataIOException;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import dev.rosewood.rosestacker.nms.v1_19_R2.NMSHandlerImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
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
    public void forEach(Consumer<LivingEntity> consumer) {
        this.forEachCapped(Integer.MAX_VALUE, consumer);
    }

    @Override
    public void forEachCapped(int count, Consumer<LivingEntity> consumer) {
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return;

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        int amount = Math.min(count, this.size);
        for (int i = 0; i < amount; i++)
            consumer.accept(nmsHandler.createEntityFromNBT(this.copy(), entity.getLocation(), false, entity.getType()));
    }

    @Override
    public List<LivingEntity> removeIf(Function<LivingEntity, Boolean> function) {
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return List.of();

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        List<LivingEntity> removedEntries = new ArrayList<>(this.size);
        for (int i = 0; i < this.size; i++) {
            LivingEntity clone = nmsHandler.createEntityFromNBT(this.copy(), entity.getLocation(), false, entity.getType());
            if (function.apply(clone))
                removedEntries.add(clone);
        }

        return removedEntries;
    }

    private NBTStackedEntityDataEntry copy() {
        CompoundTag tag = new CompoundTag();
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return new NBTStackedEntityDataEntry(tag);

        ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity, tag);
        this.stripUnneeded(tag);
        return new NBTStackedEntityDataEntry(tag);
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
        compoundTag.remove("Leash");
        compoundTag.remove("IsSitting");
        compoundTag.remove("Spigot.ticksLived");
        compoundTag.remove("Paper.OriginWorld");
        compoundTag.remove("Paper.Origin");
        CompoundTag bukkitValues = compoundTag.getCompound("BukkitValues");
        bukkitValues.remove("rosestacker:stacked_entity_data");

        // Remove any items the entity could be holding
        compoundTag.remove("ArmorItems");
        compoundTag.remove("HandItems");
        compoundTag.remove("Items");
        compoundTag.remove("ChestedHorse");
        compoundTag.remove("Saddle");
        compoundTag.remove("DecorItem");
        compoundTag.remove("Inventory");
        compoundTag.remove("carriedBlockState");

        // Remove anything else special for duplicating the entity
        compoundTag.remove("DeathTime");
        compoundTag.remove("Health");
    }

}
