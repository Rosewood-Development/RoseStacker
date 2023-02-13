package dev.rosewood.rosestacker.hook;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.compat.layers.persistentdata.MobMetaFlagType;
import org.bukkit.entity.LivingEntity;

public class OldMcMMOHook implements McMMOHook {

    @Override
    public void flagSpawnerMetadata(LivingEntity entity, boolean flag) {
        if (flag) {
            mcMMO.getCompatibilityManager().getPersistentDataLayer().flagMetadata(MobMetaFlagType.MOB_SPAWNER_MOB, entity);
        } else {
            mcMMO.getCompatibilityManager().getPersistentDataLayer().removeMobFlag(MobMetaFlagType.MOB_SPAWNER_MOB, entity);
        }
    }

}
