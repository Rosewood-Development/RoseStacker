package dev.rosewood.rosestacker.nms.storage;

import java.util.Arrays;

public enum StackedEntityDataStorageType {

    NBT(0),
    SIMPLE(1);

    private final int id;

    StackedEntityDataStorageType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static StackedEntityDataStorageType fromId(int id) {
        return Arrays.stream(StackedEntityDataStorageType.values())
                .filter(x -> x.getId() == id)
                .findFirst()
                .orElse(null);
    }

}
