package dev.rosewood.rosestacker.hologram;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import me.arasple.mc.trhologram.api.TrHologramAPI;
import me.arasple.mc.trhologram.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class TrHologramHandler implements HologramHandler {

    private final Map<Location, Hologram> holograms;

    public TrHologramHandler() {
        this.holograms = new HashMap<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        Hologram hologram = this.holograms.get(location);
        if (hologram == null) {
            hologram = TrHologramAPI.createHologram(RoseStacker.getInstance(), StackerUtils.locationAsKey(location), location, text);
            this.holograms.put(location, hologram);
        } else {
            hologram.updateLines(Collections.singletonList(text));
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
    public boolean isHologram(Entity entity) {
        return false; // TrHologram appears to use packets and therefore does not use entities
    }

}
