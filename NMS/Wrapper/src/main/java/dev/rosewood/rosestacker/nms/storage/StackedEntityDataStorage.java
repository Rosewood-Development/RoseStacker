package dev.rosewood.rosestacker.nms.storage;

import java.util.List;
import org.bukkit.entity.LivingEntity;

/**
 * Used to store large amounts of entities of the same type in a small data footprint
 */
public interface StackedEntityDataStorage {

    /**
     * Adds a new entry to the front
     *
     * @param entity The entity to add
     */
    void addFirst(LivingEntity entity);

    /**
     * Adds a new entry to the end
     *
     * @param entity The entity to add
     */
    void addLast(LivingEntity entity);

    /**
     * Adds all given entries to the front
     *
     * @param stackedEntityDataEntry The WrappedNBT entries to add
     */
    void addAllFirst(List<StackedEntityDataEntry<?>> stackedEntityDataEntry);

    /**
     * Adds all given entries to the end
     *
     * @param stackedEntityDataEntry The WrappedNBT entries to add
     */
    void addAllLast(List<StackedEntityDataEntry<?>> stackedEntityDataEntry);

    /**
     * @return A WrappedNBT object for the entity at the front of the NBT list
     */
    StackedEntityDataEntry<?> peek();

    /**
     * Gets and removes an entity from the nbt list
     *
     * @return A WrappedNBT object for the entity at the front of the NBT list
     */
    StackedEntityDataEntry<?> pop();

    /**
     * @return the number of NBT entries
     */
    int size();

    /**
     * @return true if there are no NBT entries, false otherwise
     */
    boolean isEmpty();

    /**
     * @return a list of all uncompressed NBT entries
     */
    List<StackedEntityDataEntry<?>> getAll();

    /**
     * @return all compressed NBT entries serialized into a savable format
     */
    byte[] serialize();

}
