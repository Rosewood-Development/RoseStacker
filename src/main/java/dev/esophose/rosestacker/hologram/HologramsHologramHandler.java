package dev.esophose.rosestacker.hologram;

import com.sainttx.holograms.HologramPlugin;
import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.line.TextLine;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class HologramsHologramHandler implements HologramHandler {

    private HologramManager hologramManager;
    private Map<Location, Hologram> holograms;

    public HologramsHologramHandler() {
        this.hologramManager = JavaPlugin.getPlugin(HologramPlugin.class).getHologramManager();
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = new Hologram(StackerUtils.locationAsKey(location), location);
            hologram.addLine(new TextLine(hologram, text));
            this.hologramManager.addActiveHologram(hologram);
        } else {
            hologram.getLines().forEach(hologram::removeLine);
            hologram.addLine(new TextLine(hologram, text));
        }
    }

    @Override
    public void deleteHologram(Location location) {
        Hologram hologram = this.holograms.get(location);
        if (hologram != null) {
            hologram.despawn();
            this.hologramManager.deleteHologram(hologram);
            this.holograms.remove(location);
        }
    }

    @Override
    public void deleteAllHolograms() {
        this.holograms.keySet().forEach(this::deleteHologram);
    }

}
