package dev.esophose.rosestacker.hologram;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import com.Zrips.CMI.Modules.Holograms.HologramManager;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Location;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CMIHologramHandler implements HologramHandler {

    private HologramManager hologramManager;
    private Map<Location, CMIHologram> holograms;

    public CMIHologramHandler() {
        this.hologramManager = CMI.getInstance().getHologramManager();
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        CMIHologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = new CMIHologram(StackerUtils.locationAsKey(location), location);
            hologram.setLines(Collections.singletonList(text));
            this.hologramManager.addHologram(hologram);
            this.holograms.put(location, hologram);
        } else {
            hologram.setLines(Collections.singletonList(text));
        }

        hologram.update();
    }

    @Override
    public void deleteHologram(Location location) {
        CMIHologram hologram = this.holograms.get(location);
        if (hologram != null) {
            this.hologramManager.removeHolo(hologram);
            this.holograms.remove(location);
        }
    }

    @Override
    public void deleteAllHolograms() {
        this.holograms.keySet().forEach(this::deleteHologram);
    }

}
