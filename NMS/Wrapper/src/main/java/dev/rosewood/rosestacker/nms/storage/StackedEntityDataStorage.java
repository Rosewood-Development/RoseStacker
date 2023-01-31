package dev.rosewood.rosestacker.nms.storage;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.entity.LivingEntity;

/**
 * Used to store large amounts of entities of the same type in a small data footprint
 */
public abstract class StackedEntityDataStorage {

    private final StackedEntityDataStorageType type;
    protected Reference<LivingEntity> entity;

    public StackedEntityDataStorage(StackedEntityDataStorageType type, LivingEntity entity) {
        this.type = type;
        this.entity = new WeakReference<>(entity);
    }

    /**
     * @return the storage type
     */
    public StackedEntityDataStorageType getType() {
        return this.type;
    }

    /**
     * @return the entity that this storage is for
     */
    public LivingEntity getEntity() {
        return this.entity.get();
    }

    /**
     * Updates the entity used by this storage
     *
     * @param entity the new entity
     */
    public void updateEntity(LivingEntity entity) {
        this.entity = new WeakReference<>(entity);
    }

    /**
     * Adds a new entry
     *
     * @param entity The entity to add
     */
    public abstract void add(LivingEntity entity);

    /**
     * Adds all given entries
     *
     * @param stackedEntityDataEntry The StackedEntityDataEntry entries to add
     */
    public abstract void addAll(List<StackedEntityDataEntry<?>> stackedEntityDataEntry);

    /**
     * Adds a number of clones to the data storage
     *
     * @param amount The amount of clones to add
     */
    public abstract void addClones(int amount);

    /**
     * @return A StackedEntityDataEntry object for the entity at the front of the list
     */
    public abstract StackedEntityDataEntry<?> peek();

    /**
     * Gets and removes an entity from the list
     *
     * @return A StackedEntityDataEntry object for the entity at the front of the list
     */
    public abstract StackedEntityDataEntry<?> pop();

    /**
     * Gets and removes an entity from the list
     *
     * @param amount The amount of entities to remove
     * @return A List of StackedEntityDataEntry objects for the entities at the front of the list
     */
    public abstract List<StackedEntityDataEntry<?>> pop(int amount);

    /**
     * @return the number of entries
     */
    public abstract int size();

    /**
     * @return true if there are no entries, false otherwise
     */
    public abstract boolean isEmpty();

    /**
     * @return a list of all uncompressed entries
     */
    public abstract List<StackedEntityDataEntry<?>> getAll();

    /**
     * Serializes the stored entity data into a byte array
     *
     * @param maxAmount The max amount of entities to store
     * @return the compressed entries serialized into a savable format
     */
    public abstract byte[] serialize(int maxAmount);

    /**
     * @return all compressed entries serialized into a savable format
     */
    public byte[] serialize() {
        return this.serialize(Integer.MAX_VALUE);
    }

    /**
     * Calls the given consumer for each element in this storage
     *
     * @param consumer The consumer to call for each element
     */
    public void forEach(Consumer<LivingEntity> consumer) {
        this.forEachCapped(Integer.MAX_VALUE, consumer);
    }

    /**
     * Calls the given consumer for each element in this storage, up to a certain amount
     *
     * @param count the number of entries to call
     */
    public abstract void forEachCapped(int count, Consumer<LivingEntity> consumer);

    /**
     * Calls the given function for each element in this storage and removes any element where the function returns true
     *
     * @param function The function to call for each element
     * @return a list of all removed entries
     */
    public abstract List<LivingEntity> removeIf(Function<LivingEntity, Boolean> function);

    /**
     * Creates a backing queue to be used for the storage
     *
     * @return the backing queue
     * @param <T> the type of the queue
     */
    public static <T> Queue<T> createBackingQueue() {
        return new LinkedBlockingQueue<>();
    }

}
