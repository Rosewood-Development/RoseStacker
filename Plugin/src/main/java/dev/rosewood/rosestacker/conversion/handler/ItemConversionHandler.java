package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedItem;
import java.util.HashSet;
import java.util.Set;

public class ItemConversionHandler extends ConversionHandler {

    public ItemConversionHandler(RoseStacker roseStacker) {
        super(roseStacker, StackType.ITEM, false);
    }

    @Override
    public Set<Stack<?>> handleConversion(Set<ConversionData> conversionData) {
        Set<Stack<?>> stacks = new HashSet<>();

        for (ConversionData data : conversionData) {
            StackedItem stackedItem = new StackedItem(data.getStackSize(), data.getItem());
            this.stackManager.addItemStack(stackedItem);
            stacks.add(stackedItem);
        }

        return stacks;
    }

}
