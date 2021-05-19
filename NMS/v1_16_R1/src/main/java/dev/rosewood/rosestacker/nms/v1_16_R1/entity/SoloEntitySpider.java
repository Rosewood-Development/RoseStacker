package dev.rosewood.rosestacker.nms.v1_16_R1.entity;

import net.minecraft.server.v1_16_R1.DifficultyDamageScaler;
import net.minecraft.server.v1_16_R1.EntitySpider;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.EnumMobSpawn;
import net.minecraft.server.v1_16_R1.GeneratorAccess;
import net.minecraft.server.v1_16_R1.GroupDataEntity;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.World;

public class SoloEntitySpider extends EntitySpider {

    public SoloEntitySpider(EntityTypes<? extends EntitySpider> var0, World var1) {
        super(var0, var1);
    }

    @Override
    public GroupDataEntity prepare(GeneratorAccess var0, DifficultyDamageScaler var1, EnumMobSpawn var2, GroupDataEntity var3, NBTTagCompound var4) {
        return null;
    }

}
