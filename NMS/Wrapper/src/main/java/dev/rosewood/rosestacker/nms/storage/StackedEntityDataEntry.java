package dev.rosewood.rosestacker.nms.storage;

public interface StackedEntityDataEntry<T> {

    /**
     * @return the NMS NBT object
     */
    T get();

}
