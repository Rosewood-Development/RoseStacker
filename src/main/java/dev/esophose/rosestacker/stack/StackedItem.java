package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.LocaleManager.Locale;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StackedItem extends Stack {

    private int size;
    private Item item;

    public StackedItem(int id, int size, Item item) {
        super(id);

        this.size = size;
        this.item = item;

        if (Bukkit.isPrimaryThread())
            this.updateDisplay();
    }

    public StackedItem(int size, Item item) {
        this(-1, size, item);
    }

    public Item getItem() {
        return this.item;
    }

    public void increaseStackSize(int amount) {
        this.size += amount;
        this.updateDisplay();
    }

    public void setStackSize(int size) {
        this.size = size;
        this.updateDisplay();
    }

    @Override
    public int getStackSize() {
        return this.size;
    }

    @Override
    public Location getLocation() {
        return this.item.getLocation();
    }

    @Override
    public void updateDisplay() {
        ItemStack itemStack = this.item.getItemStack();
        itemStack.setAmount(Math.min(this.size, itemStack.getMaxStackSize()));
        this.item.setItemStack(itemStack);

        if (!Setting.ITEM_DISPLAY_TAGS.getBoolean())
            return;

        String displayName;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && itemMeta.hasDisplayName() && Setting.ITEM_DISPLAY_CUSTOM_NAMES.getBoolean()) {
            if (Setting.ITEM_DISPLAY_CUSTOM_NAMES_COLOR.getBoolean()) {
                displayName = itemMeta.getDisplayName();
            } else {
                displayName = ChatColor.stripColor(itemMeta.getDisplayName());
            }
        } else {
            displayName = StackerUtils.formatName(this.item.getItemStack().getType().name());
        }

        String displayString = Locale.STACK_DISPLAY.get()
                .replaceAll("%amount%", String.valueOf(this.size))
                .replaceAll("%name%", displayName);

        this.item.setCustomNameVisible(this.size > 1 || Setting.ITEM_DISPLAY_TAGS_SINGLE.getBoolean());
        this.item.setCustomName(displayString);
    }

    /**
     * Checks if this StackedItem's item is equal to another item
     *
     * @param other The other StackedItem or Item to compare
     * @return true if this StackedItem's item is equal, otherwise false
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof StackedItem)
            return this.item.equals(((StackedItem) other).item);

        if (other instanceof Item)
            return this.item.equals(other);

        return false;
    }

    /**
     * @return a hash code identical to this item for easier look up by entity
     */
    @Override
    public int hashCode() {
        return this.item.hashCode();
    }

}
