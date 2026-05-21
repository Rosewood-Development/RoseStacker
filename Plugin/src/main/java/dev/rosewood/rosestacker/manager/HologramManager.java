package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.scheduler.task.ScheduledTask;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.utils.EntityUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.util.Vector;

public class HologramManager extends Manager implements Listener {

    private final Map<Location, Hologram> holograms;
    private final NMSHandler nmsHandler;
    private ScheduledTask watcherTask;
    private double renderDistanceSqrd;
    private boolean hideThroughWalls;

    public HologramManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.holograms = new ConcurrentHashMap<>();
        this.nmsHandler = NMSAdapter.getHandler();

        Bukkit.getPluginManager().registerEvents(this, this.rosePlugin);
    }

    @Override
    public void reload() {
        this.watcherTask = this.rosePlugin.getScheduler().runTaskTimerAsync(this::updateWatchers, 0L, SettingKey.HOLOGRAM_UPDATE_FREQUENCY.get());
        this.renderDistanceSqrd = SettingKey.BLOCK_DYNAMIC_TAG_VIEW_RANGE.get() * SettingKey.BLOCK_DYNAMIC_TAG_VIEW_RANGE.get();
        this.hideThroughWalls = SettingKey.BLOCK_DYNAMIC_TAG_VIEW_RANGE_WALL_DETECTION_ENABLED.get();
    }

    @Override
    public void disable() {
        if (this.watcherTask != null) {
            this.watcherTask.cancel();
            this.watcherTask = null;
        }

        this.holograms.values().forEach(Hologram::delete);
        this.holograms.clear();
    }

    private void updateWatchers() {
        Collection<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<Hologram> holograms = new ArrayList<>(this.holograms.values());
        for (Player player : players)
            ThreadUtils.runOnEntity(player, () -> {
                for (Hologram hologram : holograms)
                    this.updateWatcher(player, hologram);
            });
    }

    private void updateWatcher(Player player, Hologram hologram) {
        if (this.isPlayerInRange(player, hologram.getLocation())) {
            hologram.addWatcher(player);
            if (this.hideThroughWalls)
                hologram.setVisibility(player, this.hasLineOfSight(player.getEyeLocation(), hologram.getDisplayLocation(), 0.75, true));
        } else {
            hologram.removeWatcher(player);
        }
    }

    private boolean isPlayerInRange(Player player, Location location) {
        return player.getWorld().equals(location.getWorld()) && player.getLocation().distanceSquared(location) <= this.renderDistanceSqrd;
    }

    private boolean hasLineOfSight(Location location1, Location location2, double accuracy, boolean requireOccluding) {
        Vector vector1 = location1.toVector();
        Vector vector2 = location2.toVector();
        double distance = vector1.distance(vector2);
        if (distance <= 0)
            return true;

        Vector direction = vector2.clone().subtract(vector1).normalize();
        double numSteps = distance / accuracy;
        double stepSize = distance / numSteps;
        for (double i = 0; i < distance; i += stepSize) {
            Location location = location1.clone().add(direction.clone().multiply(i));
            Material type = EntityUtils.getLazyBlockMaterial(location);
            if (type.isSolid() && (!requireOccluding || StackerUtils.isOccluding(type)))
                return false;
        }

        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ThreadUtils.runOnEntity(player, () -> {
            for (Hologram hologram : this.holograms.values())
                this.updateWatcher(player, hologram);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ThreadUtils.runOnEntity(player, () -> {
            for (Hologram hologram : this.holograms.values())
                hologram.removeWatcher(player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        new ArrayList<>(this.holograms.keySet()).stream()
                .filter(x -> x.getWorld().equals(event.getWorld()))
                .forEach(this::deleteHologram);
    }

    /**
     * Creates or updates a hologram at the given location
     *
     * @param location The location of the hologram
     * @param text The text for the hologram
     */
    public void createOrUpdateHologram(Location location, List<String> text) {
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = this.nmsHandler.createHologram(location, text);
            this.holograms.put(location, hologram);
            for (Player player : Bukkit.getOnlinePlayers())
                this.updateWatcherSafely(player, hologram);
        } else {
            boolean recreate = hologram.setTextSilently(text);
            for (Player player : new ArrayList<>(hologram.getWatchers()))
                this.updateTextSafely(player, hologram, recreate);
        }
    }

    /**
     * Deletes a hologram at a given location if one exists
     *
     * @param location The location of the hologram
     */
    public void deleteHologram(Location location) {
        Hologram hologram = this.holograms.get(location);
        if (hologram != null) {
            this.holograms.remove(location);
            for (Player player : new ArrayList<>(hologram.getWatchers()))
                ThreadUtils.runOnEntity(player, () -> hologram.removeWatcher(player));
        }
    }

    private void updateWatcherSafely(Player player, Hologram hologram) {
        ThreadUtils.runOnEntity(player, () -> this.updateWatcher(player, hologram));
    }

    private void updateTextSafely(Player player, Hologram hologram, boolean recreate) {
        ThreadUtils.runOnEntity(player, () -> {
            if (recreate) {
                hologram.refresh(player);
            } else {
                hologram.update(player, true);
            }
        });
    }

}
