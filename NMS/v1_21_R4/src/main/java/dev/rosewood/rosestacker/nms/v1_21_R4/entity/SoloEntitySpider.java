package dev.rosewood.rosestacker.nms.v1_21_R4.entity;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class SoloEntitySpider extends Spider {

    public SoloEntitySpider(EntityType<? extends Spider> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldaccess, DifficultyInstance difficultydamagescaler, EntitySpawnReason entityspawnreason, SpawnGroupData groupdataentity) {
        return null;
    }

}
