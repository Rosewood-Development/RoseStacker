package dev.rosewood.rosestacker.hologram;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import com.Zrips.CMI.Modules.Holograms.HologramManager;
import com.Zrips.CMI.Modules.ModuleHandling.CMIModule;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.Zrips.CMILib.Container.CMILocation;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CMIHologramHandler implements HologramHandler {

    private final HologramManager hologramManager;
    private final Map<Location, CMIHologram> holograms;

    public CMIHologramHandler() {
        this.hologramManager = CMI.getInstance().getHologramManager();
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        CMIHologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = new CMIHologram(StackerUtils.locationAsKey(location), new CMILocation(location.clone().add(0, 1, 0)));
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

    @Override
    public boolean isHologram(Entity entity) {
        return false; // CMI Holograms appear to use packets and therefore do not use entities
    }

    @Override
    public boolean isEnabled() {
        return CMIModule.holograms.isEnabled();
    }

}
