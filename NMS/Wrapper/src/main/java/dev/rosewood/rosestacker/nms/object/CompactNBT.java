package dev.rosewood.rosestacker.nms.object;

import java.util.List;
import org.bukkit.entity.LivingEntity;

/**
 * Used to store large amounts of entities of the same type in a small data footprint
 */
public interface CompactNBT {

    void addFirst(LivingEntity entity);

    void addLast(LivingEntity entity);

    void addAllFirst(List<WrappedNBT<?>> wrappedNbt);

    void addAllLast(List<WrappedNBT<?>> wrappedNbt);

    WrappedNBT<?> peek();

    WrappedNBT<?> pop();

    int size();

    boolean isEmpty();

    List<WrappedNBT<?>> getAll();

    byte[] serialize();

}
