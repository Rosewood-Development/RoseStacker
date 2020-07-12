package dev.rosewood.rosestacker.hook;

import com.gmail.nossr50.mcMMO;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class McMMOHook {

    private static Boolean enabled;

    public static boolean enabled() {
        if (enabled != null)
            return enabled;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("mcMMO");
        return enabled = plugin != null && plugin.getDescription().getVersion().startsWith("2");
    }

    public static void updateCustomName(LivingEntity entity) {
        if (!enabled())
            return;

        entity.setMetadata(mcMMO.customNameKey, new FixedMetadataValue(mcMMO.p, String.valueOf(entity.getCustomName())));
        entity.setMetadata(mcMMO.customVisibleKey, new FixedMetadataValue(mcMMO.p, entity.isCustomNameVisible()));
    }

}
