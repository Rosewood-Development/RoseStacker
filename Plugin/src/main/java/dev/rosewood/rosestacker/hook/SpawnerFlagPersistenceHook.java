package dev.rosewood.rosestacker.hook;

import com.gamingmesh.jobs.container.JobsMobSpawner;
import com.gmail.nossr50.metadata.MobMetaFlagType;
import com.gmail.nossr50.util.MobMetadataUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseloot.util.LootUtils;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class SpawnerFlagPersistenceHook {

    private static Boolean mcMMOEnabled;
    private static Boolean jobsEnabled;
    private static Boolean roseLootEnabled;
    private static boolean displayedMcMMOMessage;

    /**
     * @return true if mcMMO is enabled, false otherwise
     */
    public static boolean mcMMOEnabled() {
        if (mcMMOEnabled != null)
            return mcMMOEnabled;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("mcMMO");
        mcMMOEnabled = plugin != null && plugin.getDescription().getVersion().startsWith("2") && NMSUtil.getVersionNumber() >= 18;
        if (mcMMOEnabled) {
            try {
                Class.forName("com.gmail.nossr50.util.MobMetadataUtils");
            } catch (ClassNotFoundException e) {
                mcMMOEnabled = false;
                if (!displayedMcMMOMessage) {
                    RoseStacker.getInstance().getLogger().severe("mcMMO is enabled, but the required com.gmail.nossr50.util.MobMetadataUtils class is not found. Your mcMMO version is either too old or too new for RoseStacker to support. The mcMMO hook has been disabled.");
                    displayedMcMMOMessage = true;
                }
            }
        }
        return mcMMOEnabled;
    }

    /**
     * @return true if Jobs is enabled, false otherwise
     */
    public static boolean jobsEnabled() {
        if (jobsEnabled != null)
            return jobsEnabled;
        return jobsEnabled = Bukkit.getPluginManager().getPlugin("Jobs") != null;
    }

    /**
     * @return true if RoseLoot is enabled, false otherwise
     */
    public static boolean roseLootEnabled() {
        if (roseLootEnabled != null)
            return roseLootEnabled;
        return roseLootEnabled = Bukkit.getPluginManager().getPlugin("RoseLoot") != null;
    }

    /**
     * Flags a LivingEntity as having been spawned from a spawner
     *
     * @param entity The LivingEntity to flag
     */
    public static void flagSpawnerSpawned(LivingEntity entity) {
        if (!SettingKey.MISC_SPAWNER_PERSISTENT_COMPATIBILITY.get())
            return;

        if (SettingKey.MISC_SPAWNER_MCMMO_COMPATIBILITY.get() && mcMMOEnabled())
            MobMetadataUtils.flagMetadata(MobMetaFlagType.MOB_SPAWNER_MOB, entity);

        if (SettingKey.MISC_SPAWNER_JOBS_COMPATIBILITY.get() && jobsEnabled())
            JobsMobSpawner.setSpawnerMeta(entity);

        if (SettingKey.MISC_SPAWNER_ROSELOOT_COMPATIBILITY.get() && roseLootEnabled())
            LootUtils.setEntitySpawnReason(entity, SpawnReason.SPAWNER);

        NMSAdapter.getHandler().setPaperFromMobSpawner(entity);
    }

    public static void unflagSpawnerSpawned(LivingEntity entity) {
        if (!SettingKey.MISC_SPAWNER_PERSISTENT_COMPATIBILITY.get())
            return;

        if (SettingKey.MISC_SPAWNER_MCMMO_COMPATIBILITY.get() && mcMMOEnabled())
            MobMetadataUtils.removeMobFlag(MobMetaFlagType.MOB_SPAWNER_MOB, entity);

        if (SettingKey.MISC_SPAWNER_JOBS_COMPATIBILITY.get() && jobsEnabled())
            JobsMobSpawner.removeSpawnerMeta(entity);
    }

    /**
     * Set's the LivingEntity's spawner persistence state if it was spawned from a spawner
     *
     * @param entity The entity to set the persistence state of
     */
    public static void setPersistence(LivingEntity entity) {
        if (!PersistentDataUtils.isSpawnedFromSpawner(entity))
            return;

        flagSpawnerSpawned(entity);
    }

}
