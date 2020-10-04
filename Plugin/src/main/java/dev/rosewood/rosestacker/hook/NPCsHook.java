package dev.rosewood.rosestacker.hook;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.EntityTracker;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.songoda.epicbosses.EpicBosses;
import io.lumine.xikage.mythicmobs.MythicMobs;
import net.citizensnpcs.api.CitizensAPI;
import net.lanus.xlm.ExtremeLevelMobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCsHook {

    private static Boolean citizensEnabled;
    private static Boolean shopkeepersEnabled;
    private static Boolean mythicMobsEnabled;
    private static Boolean epicBossesEnabled;
    private static Boolean eliteMobsEnabled;

    public static boolean citizensEnabled() {
        if (citizensEnabled != null)
            return citizensEnabled;

        return citizensEnabled = Bukkit.getPluginManager().isPluginEnabled("Citizens");
    }

    public static boolean shopkeepersEnabled() {
        if (shopkeepersEnabled != null)
            return shopkeepersEnabled;

        return shopkeepersEnabled = Bukkit.getPluginManager().isPluginEnabled("Shopkeepers");
    }

    public static boolean mythicMobsEnabled() {
        if (mythicMobsEnabled != null)
            return mythicMobsEnabled;

        return mythicMobsEnabled = Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
    }

    public static boolean epicBossesEnabled() {
        if (epicBossesEnabled != null)
            return epicBossesEnabled;

        return epicBossesEnabled = Bukkit.getPluginManager().isPluginEnabled("EpicBosses");
    }

    public static boolean eliteMobsEnabled() {
        if (eliteMobsEnabled != null)
            return eliteMobsEnabled;

        return eliteMobsEnabled = Bukkit.getPluginManager().isPluginEnabled("EliteMobs");
    }

    public static boolean anyEnabled() {
        return citizensEnabled()
                || shopkeepersEnabled()
                || mythicMobsEnabled()
                || epicBossesEnabled()
                || eliteMobsEnabled();
    }

    public static boolean isNPC(LivingEntity entity) {
        boolean npc = false;

        if (citizensEnabled())
            npc = CitizensAPI.getNPCRegistry().isNPC(entity);

        if (!npc && shopkeepersEnabled())
            npc = ShopkeepersAPI.getShopkeeperRegistry().isShopkeeper(entity);

        if (!npc && mythicMobsEnabled())
            npc = MythicMobs.inst().getAPIHelper().isMythicMob(entity);

        if (!npc && epicBossesEnabled())
            npc = EpicBosses.getInstance().getBossEntityManager().getActiveBossHolder(entity) != null;

        if (!npc && eliteMobsEnabled())
            npc = EntityTracker.isEliteMob(entity) && EntityTracker.isNPCEntity(entity);

        return npc;
    }

}
