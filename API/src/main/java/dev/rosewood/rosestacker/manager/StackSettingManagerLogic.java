package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface StackSettingManagerLogic extends ManagerLogic {

    EntityStackSettings getEntityStackSettings(EntityType entityType);

    EntityStackSettings getEntityStackSettings(Material spawnEggType);

    ItemStackSettings getItemStackSettings(Material material);

    BlockStackSettings getBlockStackSettings(Material material);

    SpawnerStackSettings getSpawnerStackSettings(EntityType entityType);

}
