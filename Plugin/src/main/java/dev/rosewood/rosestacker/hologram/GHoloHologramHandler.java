package dev.rosewood.rosestacker.hologram;

import com.google.common.collect.Lists;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import me.gholo.api.GHoloAPI;
import me.gholo.objects.Holo;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class GHoloHologramHandler implements HologramHandler {

    private final GHoloAPI api;
    private final Set<Location> locations;

    public GHoloHologramHandler() {
        this.api = new GHoloAPI();
        this.locations = new HashSet<>();
    }

    @Override
    public void createOrUpdateHologram(Location location, String text) {
        String key = StackerUtils.locationAsKey(location);
        List<String> content = Lists.newArrayList(text);
        if (this.api.getHolo(key) == null) {
            this.api.insertHolo(key, location.clone().add(0, 0.65, 0), content);
            this.locations.add(location);
        } else {
            this.api.setHoloContent(key, content);
        }
    }

    @Override
    public void deleteHologram(Location location) {
        String key = StackerUtils.locationAsKey(location);
        this.api.removeHolo(key);
        this.locations.remove(location);
    }

    @Override
    public void deleteAllHolograms() {
        new HashSet<>(this.locations).forEach(this::deleteHologram);
    }

    @Override
    public boolean isHologram(Entity entity) {
        return this.locations.stream().map(x -> this.api.getHolo(StackerUtils.locationAsKey(x)))
                .filter(Objects::nonNull)
                .map(Holo::getUUIDs)
                .flatMap(List::stream)
                .anyMatch(x -> x.equals(entity.getUniqueId()));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
