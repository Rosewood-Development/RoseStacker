package dev.rosewood.rosestacker.hook;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.compat.layers.persistentdata.MobMetaFlagType;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class McMMOHook {

    private static Boolean enabled;

    public static boolean enabled() {
        if (enabled != null)
            return enabled;
        return enabled = Bukkit.getPluginManager().getPlugin("mcMMO") != null;
    }

    public static void flagSpawnerSpawned(LivingEntity entity) {
        if (!enabled())
            return;

        mcMMO.getCompatibilityManager().getPersistentDataLayer().flagMetadata(MobMetaFlagType.MOB_SPAWNER_MOB, entity);
    }

}
