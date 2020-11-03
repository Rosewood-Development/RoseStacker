package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedItem;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class UltimateStackerConversionHandler extends ConversionHandler {

    public UltimateStackerConversionHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackType.ITEM, true);
    }

    @Override
    public Set<Stack<?>> handleConversion(Set<ConversionData> conversionData) {
        Set<Stack<?>> stacks = new HashSet<>();

        for (ConversionData data : conversionData) {
            Item item = data.getItem();
            if (item == null)
                continue;

            int stackSize = this.getItemAmount(item);
            StackedItem stackedItem = new StackedItem(stackSize, data.getItem());
            this.stackManager.addItemStack(stackedItem);
            stacks.add(stackedItem);
        }

        return stacks;
    }

    /**
     * Gets the actual item amount from the stack
     * Note: UltimateStacker doesn't even persist item stack sizes across reloads/restarts.
     *       You can legitimately lose thousands of items through a reload because it doesn't persist stack size data.
     *       This is really only useful for after the convert command is used to immediately convert loaded chunks.
     *
     * @param item The Item to get the amount of
     * @return The amount of the item
     */
    protected int getItemAmount(Item item) {
        ItemStack itemStack = item.getItemStack();
        int amount = itemStack.getAmount();
        if (item.hasMetadata("US_AMT")) {
            return item.getMetadata("US_AMT").get(0).asInt();
        } else {
            return amount;
        }
    }

}
