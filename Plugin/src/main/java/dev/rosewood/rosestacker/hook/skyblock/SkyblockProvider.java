package dev.rosewood.rosestacker.hook.skyblock;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface SkyblockProvider {

    boolean canBreakAndPlace(Player player, Location location);

}
