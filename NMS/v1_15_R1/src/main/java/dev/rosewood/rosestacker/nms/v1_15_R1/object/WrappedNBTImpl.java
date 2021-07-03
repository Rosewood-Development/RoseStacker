package dev.rosewood.rosestacker.nms.v1_15_R1.object;

import dev.rosewood.rosestacker.nms.object.WrappedNBT;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class WrappedNBTImpl implements WrappedNBT<NBTTagCompound> {

    private final NBTTagCompound compoundTag;

    public WrappedNBTImpl(NBTTagCompound compoundTag) {
        this.compoundTag = compoundTag;
    }

    @Override
    public NBTTagCompound get() {
        return this.compoundTag;
    }

}
