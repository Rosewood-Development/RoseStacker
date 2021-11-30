package dev.rosewood.rosestacker.nms.v1_18_R1.object;

import dev.rosewood.rosestacker.nms.object.WrappedNBT;
import net.minecraft.nbt.CompoundTag;

public class WrappedNBTImpl implements WrappedNBT<CompoundTag> {

    private final CompoundTag compoundTag;

    public WrappedNBTImpl(CompoundTag compoundTag) {
        this.compoundTag = compoundTag;
    }

    @Override
    public CompoundTag get() {
        return this.compoundTag;
    }

}
