package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosegarden.utils.NMSUtil;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Raid;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;

public class RaidListener implements Listener {

    private final static Set<Raid> activeRaids = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRaidTrigger(RaidTriggerEvent event) {
        activeRaids.add(event.getRaid());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRaidStop(RaidStopEvent event) {
        activeRaids.remove(event.getRaid());
    }

    /**
     * Checks if a LivingEntity is part of a raid
     *
     * @param entity The LivingEntity to check
     * @return true if the LivingEntity is part of a raid, false otherwise
     */
    public static boolean isActiveRaider(LivingEntity entity) {
        if (!(entity instanceof Raider))
            return false;

        for (Raid raid : activeRaids) {
            synchronized (raid.getRaiders()) {
                if (raid.getRaiders().contains(entity))
                    return true;
            }
        }

        return false;
    }

}
