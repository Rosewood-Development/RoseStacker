package dev.esophose.rosestacker.conversion.handler;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.ConversionData;
import dev.esophose.rosestacker.stack.StackType;
import dev.esophose.rosestacker.stack.StackedItem;
import java.util.Set;
import org.bukkit.entity.Item;

public class UltimateStackerItemConversionHandler extends UltimateStackerConversionHandler {

    public UltimateStackerItemConversionHandler(RoseStacker roseStacker) {
        super(roseStacker, StackType.ITEM, true);
    }

    @Override
    public void handleConversion(Set<ConversionData> conversionData) {
        for (ConversionData data : conversionData) {
            Item item = data.getItem();
            int stackSize = this.getItemAmount(item);
            if (stackSize >= item.getItemStack().getMaxStackSize())
                continue;

            this.stackManager.addItemStack(new StackedItem(data.getStackSize(), data.getItem()));
        }
    }

}
