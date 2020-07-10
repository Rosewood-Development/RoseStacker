package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedItem;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Item;

public class UltimateStackerItemConversionHandler extends UltimateStackerConversionHandler {

    public UltimateStackerItemConversionHandler(RoseStacker roseStacker) {
        super(roseStacker, StackType.ITEM, true);
    }

    @Override
    public Set<Stack<?>> handleConversion(Set<ConversionData> conversionData) {
        Set<Stack<?>> stacks = new HashSet<>();

        for (ConversionData data : conversionData) {
            Item item = data.getItem();
            int stackSize = this.getItemAmount(item);
            if (stackSize >= item.getItemStack().getMaxStackSize())
                continue;

            StackedItem stackedItem = new StackedItem(data.getStackSize(), data.getItem());
            this.stackManager.addItemStack(stackedItem);
            stacks.add(stackedItem);
        }

        return stacks;
    }

}
