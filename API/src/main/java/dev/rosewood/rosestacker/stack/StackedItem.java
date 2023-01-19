package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import org.bukkit.entity.Item;

public interface StackedItem extends Stack<ItemStackSettings> {

    Item getItem();

    void setStackSize(int size);

}
