package dev.rosewood.rosestacker.nms.object;

public interface WrappedNBT<T> {

    /**
     * @return the NMS NBT object
     */
    T get();

}
