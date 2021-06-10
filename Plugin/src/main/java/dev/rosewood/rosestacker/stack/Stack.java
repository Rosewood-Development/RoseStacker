package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.stack.settings.StackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class Stack<T extends StackSettings> {

    private final int id;

    public Stack(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public abstract int getStackSize();

    public abstract Location getLocation();

    public abstract void updateDisplay();

    public abstract T getStackSettings();

    /**
     * @return this Stack's World
     * @throws IllegalStateException if the World is null
     */
    public World getWorld() {
        Location location = this.getLocation();
        World world = location.getWorld();
        if (world == null)
            throw new IllegalStateException("Stack world is null");
        return world;
    }

    protected Set<Player> getPlayersInVisibleRange() {
        Set<Player> players = new HashSet<>();

        Location location = this.getLocation();
        World world = location.getWorld();
        if (world == null)
            return players;

        for (Player player : world.getPlayers())
            if (player.getLocation().distanceSquared(location) <= StackerUtils.ASSUMED_ENTITY_VISIBILITY_RANGE)
                players.add(player);

        return players;
    }

}
