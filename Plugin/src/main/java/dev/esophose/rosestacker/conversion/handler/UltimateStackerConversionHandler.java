package dev.esophose.rosestacker.conversion.handler;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.stack.StackType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public abstract class UltimateStackerConversionHandler extends ConversionHandler {

    public UltimateStackerConversionHandler(RoseStacker roseStacker, StackType requiredDataStackType, boolean alwaysRequire) {
        super(roseStacker, requiredDataStackType, alwaysRequire);
    }

    /**
     * Gets the entity stack size FROM THE CUSTOM NAMETAG. WHY WOULD YOU DO THIS???
     *
     * @param entity The entity to get the stack size of
     * @return The entity stack size, or -1 if it couldn't be determined
     */
    protected int getEntityAmount(LivingEntity entity) {
        String customName = entity.getCustomName();
        if (customName != null && customName.contains(String.valueOf(ChatColor.COLOR_CHAR))) {
            String name = customName.replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace(";", "");
            if (!name.contains(":"))
                return 1;

            try {
                return Integer.parseInt(name.split(":")[0]);
            } catch (NumberFormatException ex) {
                return 1;
            }
        }
        return 1;
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
        if (amount >= itemStack.getMaxStackSize() / 2 && item.hasMetadata("US_AMT")) {
            return item.getMetadata("US_AMT").get(0).asInt();
        } else {
            return amount;
        }
    }

}
