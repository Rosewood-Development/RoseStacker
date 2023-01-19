package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.stack.settings.StackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class AbstractStack<T extends StackSettings> implements Stack<T> {

    public abstract void updateDisplay();

    protected Set<Player> getPlayersInVisibleRange() {
        Set<Player> players = new HashSet<>();

        Location location = this.getLocation();
        World world = location.getWorld();
        if (world == null)
            return players;

        for (Player player : world.getPlayers())
            if (player.getWorld().equals(world) && player.getLocation().distanceSquared(location) <= StackerUtils.ASSUMED_ENTITY_VISIBILITY_RANGE)
                players.add(player);

        return players;
    }

}
