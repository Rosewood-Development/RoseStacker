package dev.rosewood.rosestacker.hologram;

import dev.rosewood.rosestacker.utils.StackerUtils;
import eu.decentsoftware.holograms.api.DecentHologramsProvider;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.managers.HologramManager;
import eu.decentsoftware.holograms.core.holograms.DefaultHologram;
import eu.decentsoftware.holograms.core.holograms.DefaultHologramLine;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class DecentHologramsHandler implements HologramHandler {

    private final HologramManager manager;
    private final Map<Location, Hologram> holograms;

    public DecentHologramsHandler() {
        this.manager = DecentHologramsProvider.getDecentHolograms().getHologramManager();
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        String key = StackerUtils.locationAsKey(location);
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = new DefaultHologram(key, location.clone().add(0, 1, 0), false);
            hologram.addLine(new DefaultHologramLine(location, text));
            this.manager.registerHologram(hologram);
            this.holograms.put(location, hologram);
        } else {
            hologram.getLine(0).setContent(text);
            hologram.getLine(0).update();
        }
    }

    @Override
    public void deleteHologram(Location location) {
        String key = StackerUtils.locationAsKey(location);
        this.holograms.remove(location);
        if (this.manager.containsHologram(key))
            this.manager.removeHologram(key);
    }

    @Override
    public void deleteAllHolograms() {
        new HashSet<>(this.holograms.keySet()).forEach(this::deleteHologram);
    }

    @Override
    public boolean isHologram(Entity entity) {
        return false; // Packet based, no entities registered
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
