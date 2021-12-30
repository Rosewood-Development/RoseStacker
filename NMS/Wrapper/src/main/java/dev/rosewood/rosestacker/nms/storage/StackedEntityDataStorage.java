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
     * @param stackedEntityDataEntry The StackedEntityDataEntry entries to add
     */
    void addAllFirst(List<StackedEntityDataEntry<?>> stackedEntityDataEntry);

    /**
     * Adds all given entries to the end
     *
     * @param stackedEntityDataEntry The StackedEntityDataEntry entries to add
     */
    void addAllLast(List<StackedEntityDataEntry<?>> stackedEntityDataEntry);

    /**
     * @return A StackedEntityDataEntry object for the entity at the front of the list
     */
    StackedEntityDataEntry<?> peek();

    /**
     * Gets and removes an entity from the list
     *
     * @return A StackedEntityDataEntry object for the entity at the front of the list
     */
    StackedEntityDataEntry<?> pop();

    /**
     * @return the number of entries
     */
    int size();

    /**
     * @return true if there are no entries, false otherwise
     */
    boolean isEmpty();

    /**
     * @return a list of all uncompressed entries
     */
    List<StackedEntityDataEntry<?>> getAll();

    /**
     * @return all compressed entries serialized into a savable format
     */
    byte[] serialize();

}
