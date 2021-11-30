package dev.rosewood.rosestacker.hologram;

import dev.rosewood.rosestacker.utils.StackerUtils;
import eu.decentsoftware.holograms.api.DecentHolograms;
import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class DecentHologramsHandler implements HologramHandler {

    private final DecentHolograms decentHolograms;
    private final Map<Location, Hologram> holograms;

    public DecentHologramsHandler() {
        this.decentHolograms = DecentHologramsAPI.get();
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        String key = StackerUtils.locationAsKey(location);
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = new Hologram(key, location.clone().add(0, 1, 0), false);
            HologramPage page = hologram.getPage(0);
            page.addLine(new HologramLine(page, location, text));
            this.decentHolograms.getHologramManager().registerHologram(hologram);
            this.holograms.put(location, hologram);
        } else {
            HologramLine line = hologram.getPage(0).getLine(0);
            line.setContent(text);
            line.update();
        }
    }

    @Override
    public void deleteHologram(Location location) {
        String key = StackerUtils.locationAsKey(location);
        this.holograms.remove(location);
        if (this.decentHolograms.getHologramManager().containsHologram(key))
            this.decentHolograms.getHologramManager().removeHologram(key);
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
