package dev.rosewood.rosestacker.stack;

import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface StackingThread extends StackingLogic, AutoCloseable {

    /**
     * @return the world that this StackingThread is acting on
     */
    World getTargetWorld();

}
