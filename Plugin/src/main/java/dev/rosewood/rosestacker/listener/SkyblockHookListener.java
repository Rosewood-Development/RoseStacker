package dev.rosewood.rosestacker.listener;

import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.StackGUIOpenEvent;
import dev.rosewood.rosestacker.hook.skyblock.SkyblockProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkyblockHookListener implements Listener {

    private final SkyblockProvider skyblockProvider;

    public SkyblockHookListener() {
        this.skyblockProvider = null;
    }

    public boolean isEnabled() {
        return this.skyblockProvider != null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStackGuiOpen(StackGUIOpenEvent event) {
        if (this.skyblockProvider != null && !this.skyblockProvider.canBreakAndPlace(event.getPlayer(), event.getStack().getLocation()))
            event.setCancelled(true);
    }

}
