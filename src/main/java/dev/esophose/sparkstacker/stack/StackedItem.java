package dev.esophose.sparkstacker.stack;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.manager.ConfigurationManager.Setting;
import dev.esophose.sparkstacker.manager.LocaleManager.Locale;
import dev.esophose.sparkstacker.stack.settings.ItemStackSettings;
import dev.esophose.sparkstacker.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StackedItem extends Stack {

    private int size;
    private Item item;

    private ItemStackSettings stackSettings;

    public StackedItem(int id, int size, Item item) {
        super(id);

        this.size = size;
        this.item = item;

        if (this.item != null) {
            this.stackSettings = SparkStacker.getInstance().getStackSettingManager().getItemStackSettings(this.item);

            if (Bukkit.isPrimaryThread())
                this.updateDisplay();
        }
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
            displayName = this.stackSettings.getDisplayName();
        }

        String displayString = ChatColor.translateAlternateColorCodes('&', StringPlaceholders.builder("amount", this.getStackSize())
                .addPlaceholder("name", displayName)
                .apply(Locale.ITEM_STACK_DISPLAY.get()));

        this.item.setCustomNameVisible(this.size > 1 || Setting.ITEM_DISPLAY_TAGS_SINGLE.getBoolean());
        this.item.setCustomName(displayString);
    }

}
