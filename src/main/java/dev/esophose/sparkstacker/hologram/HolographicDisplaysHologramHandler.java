package dev.esophose.sparkstacker.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import dev.esophose.sparkstacker.SparkStacker;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Location;

public class HolographicDisplaysHologramHandler implements HologramHandler {

    private Map<Location, Hologram> holograms;

    public HolographicDisplaysHologramHandler() {
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(SparkStacker.getInstance(), location.clone().add(0, 1, 0));
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
        new HashSet<>(this.holograms.keySet()).forEach(this::deleteHologram);
    }

}
