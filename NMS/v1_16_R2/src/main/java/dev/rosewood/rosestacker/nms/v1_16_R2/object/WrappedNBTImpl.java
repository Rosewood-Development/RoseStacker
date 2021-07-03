package dev.rosewood.rosestacker.nms.v1_16_R2.object;

import dev.rosewood.rosestacker.nms.object.WrappedNBT;
import net.minecraft.server.v1_16_R2.NBTTagCompound;

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
