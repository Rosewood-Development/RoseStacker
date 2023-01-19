package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosestacker.stack.StackingLogic;
import dev.rosewood.rosestacker.stack.StackingThread;
import java.util.Map;
import java.util.UUID;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface StackManagerLogic extends ManagerLogic, StackingLogic {

    boolean isEntityStackMultipleDeathEventCalled();

    StackingThread getStackingThread(World world);

    Map<UUID, StackingThread> getStackingThreads();

}
