package dev.esophose.rosestacker.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import dev.esophose.rosestacker.RoseStacker;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class HolographicDisplaysHologramHandler implements HologramHandler {

    private Map<Location, Hologram> holograms;

    public HolographicDisplaysHologramHandler() {
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(RoseStacker.getInstance(), location);
            hologram.appendTextLine(text);
            this.holograms.put(location, hologram);
        } else {
            hologram.clearLines();
            hologram.appendTextLine(text);
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
        this.holograms.keySet().forEach(this::deleteHologram);
    }

}
