package dev.rosewood.rosestacker.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import dev.rosewood.rosestacker.RoseStacker;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Location;

@SuppressWarnings("deprecation") // Suppressed until the new API is out of beta
public class HolographicDisplaysHologramHandler implements HologramHandler {

    private final Map<Location, Hologram> holograms;

    public HolographicDisplaysHologramHandler() {
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(RoseStacker.getInstance(), location.clone().add(0, 1, 0));
            hologram.appendTextLine(text);
            this.holograms.put(location, hologram);
        } else {
            ((TextLine) hologram.getLine(0)).setText(text);
        }
    }

    @Override
    public void deleteHologram(Location location) {
        Hologram hologram = this.holograms.get(location);
        if (hologram != null) {
            hologram.delete();
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
