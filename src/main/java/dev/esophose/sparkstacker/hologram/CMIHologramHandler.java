package dev.esophose.sparkstacker.hologram;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMILocation;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import com.Zrips.CMI.Modules.Holograms.HologramManager;
import dev.esophose.sparkstacker.utils.StackerUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Location;

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
            hologram = new CMIHologram(StackerUtils.locationAsKey(location), new CMILocation(location));
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
        new HashSet<>(this.holograms.keySet()).forEach(this::deleteHologram);
    }

}
