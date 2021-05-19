package dev.rosewood.rosestacker.nms.v1_16_R2.entity;

import net.minecraft.server.v1_16_R2.DifficultyDamageScaler;
import net.minecraft.server.v1_16_R2.EntityStrider;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.EnumMobSpawn;
import net.minecraft.server.v1_16_R2.GroupDataEntity;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.World;
import net.minecraft.server.v1_16_R2.WorldAccess;

public class SoloEntityStrider extends EntityStrider {

    public SoloEntityStrider(EntityTypes<? extends EntityStrider> var0, World var1) {
        super(var0, var1);
    }

    @Override
    public GroupDataEntity prepare(WorldAccess var0, DifficultyDamageScaler var1, EnumMobSpawn var2, GroupDataEntity var3, NBTTagCompound var4) {
        return null;
    }

}
