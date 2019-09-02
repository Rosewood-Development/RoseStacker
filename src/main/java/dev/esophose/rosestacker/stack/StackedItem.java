package dev.esophose.rosestacker.stack;

import dev.esophose.rosestacker.manager.LocaleManager.Locale;
import dev.esophose.rosestacker.utils.StackerUtils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class StackedItem extends Stack {

    private int size;
    private Item item;

    public StackedItem(int id, int size, Item item) {
        super(id);

        this.size = size;
        this.item = item;

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
        String displayString = Locale.STACK_DISPLAY.get()
                .replaceAll("%amount%", String.valueOf(this.size))
                .replaceAll("%name%", StackerUtils.formatName(this.item.getItemStack().getType().name()));

        this.item.setCustomNameVisible(this.size > 1);
        this.item.setCustomName(displayString);

        ItemStack itemStack = this.item.getItemStack();
        itemStack.setAmount(Math.min(this.size, itemStack.getMaxStackSize()));
        this.item.setItemStack(itemStack);
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
