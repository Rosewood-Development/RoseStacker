package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public final class StackerArgumentHandlers {

    private static final RosePlugin ROSE_PLUGIN = RoseStacker.getInstance();
    public static final ArgumentHandler<Integer> STACKED_BLOCK_AMOUNT = new StackedBlockAmountArgumentHandler(ROSE_PLUGIN);
    public static final ArgumentHandler<Material> STACKED_BLOCK_TYPE = new StackedBlockTypeArgumentHandler(ROSE_PLUGIN);
    public static final ArgumentHandler<Integer> STACKED_ENTITY_AMOUNT = new StackedEntityAmountArgumentHandler(ROSE_PLUGIN);
    public static final ArgumentHandler<EntityType> STACKED_ENTITY_TYPE = new StackedEntityTypeArgumentHandler(ROSE_PLUGIN);
    public static final ArgumentHandler<Integer> STACKED_SPAWNER_AMOUNT = new StackedSpawnerAmountArgumentHandler(ROSE_PLUGIN);
    public static final ArgumentHandler<SpawnerType> STACKED_SPAWNER_TYPE = new StackedSpawnerTypeArgumentHandler(ROSE_PLUGIN);

    private StackerArgumentHandlers() { }

}
