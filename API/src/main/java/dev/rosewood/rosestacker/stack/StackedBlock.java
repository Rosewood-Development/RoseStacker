package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import org.bukkit.block.Block;

public interface StackedBlock extends Stack<BlockStackSettings>, Viewable {

    Block getBlock();

    boolean isLocked();

}
