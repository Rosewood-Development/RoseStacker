package dev.rosewood.rosestacker.hook;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.LivingEntity;

public class OldMythicMobsHook implements MythicMobsHook {

    @Override
    public boolean isMythicMob(LivingEntity entity) {
        return MythicMobs.inst().getAPIHelper().isMythicMob(entity);
    }

}
