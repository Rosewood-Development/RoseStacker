package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedItem;
import java.util.Set;

public class ItemConversionHandler extends ConversionHandler {

    public ItemConversionHandler(RoseStacker roseStacker) {
        super(roseStacker, StackType.ITEM, false);
    }

    @Override
    public void handleConversion(Set<ConversionData> conversionData) {
        for (ConversionData data : conversionData)
            this.stackManager.addItemStack(new StackedItem(data.getStackSize(), data.getItem()));
    }

}
