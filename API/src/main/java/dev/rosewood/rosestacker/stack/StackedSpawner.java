package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

public interface StackedSpawner extends Stack<SpawnerStackSettings>, Viewable {

    StackedSpawnerTile getSpawnerTile();

    CreatureSpawner getSpawner();

    Block getBlock();

    boolean isPlacedByPlayer();

}
