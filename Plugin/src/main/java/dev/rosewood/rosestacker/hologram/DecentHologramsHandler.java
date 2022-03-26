package dev.rosewood.rosestacker.hologram;

import dev.rosewood.rosestacker.utils.StackerUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class DecentHologramsHandler implements HologramHandler {

    private final Map<Location, Hologram> holograms;

    public DecentHologramsHandler() {
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        String key = StackerUtils.locationAsKey(location);
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = DHAPI.createHologram(key, location.clone().add(0, 1, 0), false, Collections.singletonList(text));
            this.holograms.put(location, hologram);
        } else {
            DHAPI.setHologramLine(hologram, 0, text);
        }
    }

    @Override
    public void deleteHologram(Location location) {
        Hologram hologram = this.holograms.get(location);
        if (hologram != null) {
            hologram.destroy();
            this.holograms.remove(location);
        }
    }

    @Override
    public void deleteAllHolograms() {
        new HashSet<>(this.holograms.keySet()).forEach(this::deleteHologram);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
