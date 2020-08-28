package dev.rosewood.rosestacker.hook;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class CitizensHook {

    private static Boolean enabled;

    public static boolean enabled() {
        if (enabled != null)
            return enabled;

        return enabled = Bukkit.getPluginManager().isPluginEnabled("Citizens");
    }

    public static boolean isCitizen(LivingEntity entity) {
        if (!enabled())
            return false;

        return CitizensAPI.getNPCRegistry().isNPC(entity);
    }

}
