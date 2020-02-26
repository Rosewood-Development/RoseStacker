package dev.esophose.rosestacker.hologram;

import com.gmail.filoghost.holographicdisplays.HolographicDisplays;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.entity.NMSEntityBase;
import dev.esophose.rosestacker.RoseStacker;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class HolographicDisplaysHologramHandler implements HologramHandler {

    private Map<Location, Hologram> holograms;

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

    @Override
    public boolean isHologram(Entity entity) {
        NMSEntityBase entityBase = HolographicDisplays.getNMSManager().getNMSEntityBase(entity);
        if (entityBase == null)
            return false;

        HologramLine hologramLine = entityBase.getHologramLine();
        if (hologramLine == null)
            return false;

        Hologram target = entityBase.getHologramLine().getParent();
        if (target == null)
            return false;

        return this.holograms.values().stream().anyMatch(target::equals);
    }

}
