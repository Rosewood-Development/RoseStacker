package dev.rosewood.rosestacker.nms.spawner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a type of entity that can be spawned by a spawner, may be empty.
 * Can contain multiple EntityTypes.
 * <p>
 * This is a rough intermediary for the BaseSpawner spawn potentials.
 * It is currently lacking entry weights and being able to spawn entities from NBT strings.
 * </p>
 */
@ApiStatus.Experimental
public final class SpawnerType {

    private static final SpawnerType EMPTY = new SpawnerType();

    private final List<EntityType> entityTypes;
    private int activeIndex;

    private SpawnerType() {
        this.entityTypes = List.of();
    }

    private SpawnerType(EntityType entityType) {
        this.entityTypes = List.of(entityType);
    }

    private SpawnerType(Collection<EntityType> entityTypes) {
        this.entityTypes = List.copyOf(entityTypes);
    }

    /**
     * @return the next type of entity to be spawned
     */
    public Optional<EntityType> get() {
        if (this.entityTypes.isEmpty())
            return Optional.empty();

        return Optional.of(this.entityTypes.get(this.activeIndex));
    }

    /**
     * @return the next type of entity to be spawned
     * @throws IllegalStateException if the spawner type is empty
     */
    public EntityType getOrThrow() {
        return this.get().orElseThrow(() -> new IllegalStateException("Check isEmpty() before calling getOrThrow()"));
    }

    /**
     * Gets the next type of entity to be spawned and updates the current type to the next one.
     * Must check isEmpty() before calling this method.
     *
     * @return the next type of entity to be spawned
     * @throws IllegalStateException if the spawner type is empty
     */
    public EntityType next() {
        if (this.entityTypes.isEmpty())
            throw new IllegalStateException("Check isEmpty() before calling next()");

        this.activeIndex = (this.activeIndex + 1) % this.entityTypes.size();
        return this.entityTypes.get(this.activeIndex);
    }

    /**
     * @return true if this spawner type is empty
     */
    public boolean isEmpty() {
        return this.entityTypes.isEmpty();
    }

    /**
     * @return the number of entity types this spawner type contains
     */
    public int size() {
        return this.entityTypes.size();
    }

    /**
     * @return an unmodifiable list of entity types this spawner type contains
     */
    public List<EntityType> getEntityTypes() {
        return this.entityTypes;
    }

    /**
     * @return a String representation of the current spawned EntityType enum (or "EMPTY" if empty)
     */
    public String getEnumName() {
        return this.get().map(Enum::name).orElse("EMPTY");
    }

    /**
     * @return a SpawnerType with a specific EntityType
     */
    public static SpawnerType of(EntityType entityType) {
        if (entityType == null || entityType == EntityType.UNKNOWN)
            return empty();

        return new SpawnerType(entityType);
    }

    /**
     * @return a SpawnerType with one or more EntityTypes
     */
    public static SpawnerType of(EntityType... entityTypes) {
        return new SpawnerType(Arrays.asList(entityTypes));
    }

    /**
     * @return a SpawnerType with one or more EntityTypes
     */
    public static SpawnerType of(Collection<EntityType> entityTypes) {
        if (entityTypes.isEmpty())
            return empty();

        Set<EntityType> filteredTypes = entityTypes.stream().filter(x -> x != null && x != EntityType.UNKNOWN).collect(Collectors.toSet());
        return new SpawnerType(filteredTypes);
    }

    /**
     * @return the empty spawner type
     */
    public static SpawnerType empty() {
        return EMPTY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof SpawnerType other))
            return false;
        return this.entityTypes.equals(other.entityTypes);
    }

    @Override
    public int hashCode() {
        return this.entityTypes.hashCode();
    }

}
