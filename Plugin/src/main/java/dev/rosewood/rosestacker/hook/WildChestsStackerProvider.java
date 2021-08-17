package dev.rosewood.rosestacker.hook;

import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.hooks.StackerProvider;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import org.bukkit.entity.Item;

public class WildChestsStackerProvider implements StackerProvider {

    public static void register() {
        WildChestsAPI.getInstance().getProviders().setStackerProvider(new WildChestsStackerProvider());
    }

    @Override
    public int getItemAmount(Item item) {
        StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(item);
        return stackedItem != null ? stackedItem.getStackSize() : item.getItemStack().getAmount();
    }

    @Override
    public void setItemAmount(Item item, int amount) {
        StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(item);
        if (stackedItem != null) {
            stackedItem.setStackSize(amount);
        } else {
            item.getItemStack().setAmount(amount);
        }
    }

}
