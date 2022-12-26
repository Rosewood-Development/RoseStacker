package dev.rosewood.rosestacker.nms.v1_19_R2.storage;

import dev.rosewood.rosestacker.nms.storage.StackedEntityDataEntry;
import net.minecraft.nbt.CompoundTag;

public class NBTStackedEntityDataEntry implements StackedEntityDataEntry<CompoundTag> {

    private final CompoundTag compoundTag;

    public NBTStackedEntityDataEntry(CompoundTag compoundTag) {
        this.compoundTag = compoundTag;
    }

    @Override
    public CompoundTag get() {
        return this.compoundTag;
    }

}
