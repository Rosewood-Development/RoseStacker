package dev.rosewood.rosestacker.hologram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import me.arasple.mc.trhologram.api.TrHologramAPI;
import me.arasple.mc.trhologram.module.display.Hologram;
import org.bukkit.Location;

public class TrHologramHandler implements HologramHandler {

    private final Map<Location, Hologram> holograms;

    public TrHologramHandler() {
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = TrHologramAPI.builder(location.clone().add(0, 1.0, 0))
                    .append(text)
                    .build();
            this.holograms.put(location, hologram);
        } else {
            hologram.getTextLine(0).setText(text);
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
