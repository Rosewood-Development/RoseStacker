package dev.rosewood.rosestacker.nms.storage;

import java.util.Arrays;

public enum StackedEntityDataStorageType {

    NBT(0, "Store all NBT data for internal entities"),
    SIMPLE(1, "Store only the stack size and clone the main entity");

    private final int id;
    private final String description;

    StackedEntityDataStorageType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public static StackedEntityDataStorageType fromId(int id) {
        return Arrays.stream(StackedEntityDataStorageType.values())
                .filter(x -> x.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public static StackedEntityDataStorageType fromName(String name) {
        return Arrays.stream(StackedEntityDataStorageType.values())
                .filter(x -> x.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(StackedEntityDataStorageType.NBT);
    }

}
