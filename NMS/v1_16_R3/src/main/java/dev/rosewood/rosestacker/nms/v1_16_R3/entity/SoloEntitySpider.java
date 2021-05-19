package dev.rosewood.rosestacker.nms.v1_16_R3.entity;

import net.minecraft.server.v1_16_R3.DifficultyDamageScaler;
import net.minecraft.server.v1_16_R3.EntitySpider;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.GroupDataEntity;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldAccess;

public class SoloEntitySpider extends EntitySpider {

    public SoloEntitySpider(EntityTypes<? extends EntitySpider> var0, World var1) {
        super(var0, var1);
    }

    @Override
    public GroupDataEntity prepare(WorldAccess var0, DifficultyDamageScaler var1, EnumMobSpawn var2, GroupDataEntity var3, NBTTagCompound var4) {
        return null;
    }

}
