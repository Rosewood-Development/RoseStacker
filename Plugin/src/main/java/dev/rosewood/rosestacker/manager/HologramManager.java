package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosestacker.hologram.CMIHologramHandler;
import dev.rosewood.rosestacker.hologram.HologramHandler;
import dev.rosewood.rosestacker.hologram.HologramsHologramHandler;
import dev.rosewood.rosestacker.hologram.HolographicDisplaysHologramHandler;
import dev.rosewood.rosestacker.hologram.TrHologramHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class HologramManager extends Manager {

    private final Map<String, Class<? extends HologramHandler>> hologramHandlers;
    private HologramHandler hologramHandler;

    public HologramManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.hologramHandlers = new LinkedHashMap<String, Class<? extends HologramHandler>>() {{
            this.put("HolographicDisplays", HolographicDisplaysHologramHandler.class);
            this.put("Holograms", HologramsHologramHandler.class);
            this.put("CMI", CMIHologramHandler.class);
            this.put("TrHologram", TrHologramHandler.class);
        }};
    }

    @Override
    public void reload() {
        for (Map.Entry<String, Class<? extends HologramHandler>> handler : this.hologramHandlers.entrySet()) {
            if (Bukkit.getPluginManager().isPluginEnabled(handler.getKey())) {
                try {
                    this.hologramHandler = handler.getValue().getConstructor().newInstance();
                    this.rosePlugin.getLogger().info(String.format("%s is being used as the Hologram Handler.", handler.getKey()));
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (this.hologramHandler == null) {
            String validPlugins = String.join(", ", this.hologramHandlers.keySet());
            this.rosePlugin.getLogger().warning("No Hologram Handler plugin was detected. " +
                    "If you want stack tags to be displayed above stacked spawners or blocks, " +
                    "please install one of the following plugins: [" + validPlugins + "]");
        }
    }

    @Override
    public void disable() {
        if (this.hologramHandler != null) {
            this.hologramHandler.deleteAllHolograms();
            this.hologramHandler = null;
        }
    }

    /**
     * Creates or updates a hologram at the given location
     *
     * @param location The location of the hologram
     * @param text The text for the hologram
     */
    public void createOrUpdateHologram(Location location, String text) {
        if (this.hologramHandler != null)
            this.hologramHandler.createOrUpdateHologram(location, text);
    }

    /**
     * Deletes a hologram at a given location if one exists
     *
     * @param location The location of the hologram
     */
    public void deleteHologram(Location location) {
        if (this.hologramHandler != null)
            this.hologramHandler.deleteHologram(location);
    }

    /**
     * Deletes all holograms
     */
    public void deleteAllHolograms() {
        if (this.hologramHandler != null)
            this.hologramHandler.deleteAllHolograms();
    }

    /**
     * Checks if the given Entity is part of a hologram
     *
     * @param entity The Entity to check
     * @return true if the Entity is a hologram, otherwise false
     */
    public boolean isHologram(Entity entity) {
        if (this.hologramHandler != null)
            return this.hologramHandler.isHologram(entity);
        return false;
    }

}
