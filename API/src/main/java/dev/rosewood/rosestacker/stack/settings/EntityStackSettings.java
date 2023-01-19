package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface EntityStackSettings extends StackSettings {

    ItemStack getSkullItem();

    boolean isValidBreedingMaterial(Material material);

    /**
     * Checks if one StackedEntity can stack with another and returns the comparison result
     *
     * @param stack1 The first stack
     * @param stack2 The second stack
     * @param comparingForUnstack true if the comparison is being made for unstacking, false otherwise
     * @param ignorePositions true if position checks for the entities should be ignored, false otherwise
     * @return the comparison result
     */
    EntityStackComparisonResult canStackWith(StackedEntity stack1, StackedEntity stack2, boolean comparingForUnstack, boolean ignorePositions);

    int getMinStackSize();

    boolean shouldKillEntireStackOnDeath();

    double getMergeRadius();

    boolean shouldOnlyStackFromSpawners();

    EntityType getEntityType();

    EntityTypeData getEntityTypeData();

}
