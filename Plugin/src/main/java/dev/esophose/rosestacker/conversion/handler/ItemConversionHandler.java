package dev.esophose.rosestacker.conversion.handler;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.ConversionData;
import dev.esophose.rosestacker.stack.StackType;
import dev.esophose.rosestacker.stack.StackedItem;
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
