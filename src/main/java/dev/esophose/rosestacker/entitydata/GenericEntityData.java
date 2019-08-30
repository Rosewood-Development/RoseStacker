package dev.esophose.rosestacker.entitydata;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.function.Consumer;

public abstract class GenericEntityData<T extends LivingEntity> implements EntityData {

    public GenericEntityData(T entity) {
        // TODO: Keep track of everything that all LivingEntitys have, from health to potion effects
    }

    public T spawnAt(Location location) {
        return this.spawnAtInternal(location, (entity) -> {
            // TODO: Apply properties to the entity
        });
    }

    protected abstract T spawnAtInternal(Location location, Consumer<T> action);

}
