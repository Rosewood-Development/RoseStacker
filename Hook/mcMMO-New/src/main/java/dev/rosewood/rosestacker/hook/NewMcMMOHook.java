package dev.rosewood.rosestacker.hook;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.metadata.MobMetaFlagType;
import org.bukkit.entity.LivingEntity;

public class NewMcMMOHook implements McMMOHook {

    @Override
    public void flagSpawnerMetadata(LivingEntity entity, boolean flag) {
        if (flag) {
            mcMMO.getMetadataService().getMobMetadataService().flagMetadata(MobMetaFlagType.MOB_SPAWNER_MOB, entity);
        } else {
            mcMMO.getMetadataService().getMobMetadataService().removeMobFlag(MobMetaFlagType.MOB_SPAWNER_MOB, entity);
        }
    }

}
