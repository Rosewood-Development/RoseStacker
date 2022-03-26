package dev.rosewood.rosestacker.hook;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.LivingEntity;

public class NewMythicMobsHook implements MythicMobsHook {

    @Override
    public boolean isMythicMob(LivingEntity entity) {
        return MythicBukkit.inst().getAPIHelper().isMythicMob(entity);
    }

}
