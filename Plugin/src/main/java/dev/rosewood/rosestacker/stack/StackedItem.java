package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StackedItem extends Stack implements Comparable<StackedItem> {

    private int size;
    private Item item;

    private ItemStackSettings stackSettings;

    public StackedItem(int id, int size, Item item) {
        super(id);

        this.size = size;
        this.item = item;

        if (this.item != null) {
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getItemStackSettings(this.item);

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

    public void updateItem() {
        Item item = (Item) Bukkit.getEntity(this.item.getUniqueId());
        if (item == null || item == this.item)
            return;

        this.item = item;
        this.updateDisplay();
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
        if (itemStack.getType() == Material.AIR)
            return;

        itemStack.setAmount(Math.min(this.size, itemStack.getMaxStackSize()));
        this.item.setItemStack(itemStack);

        if (!Setting.ITEM_DISPLAY_TAGS.getBoolean() || this.stackSettings == null)
            return;

        String displayName;
        ItemMeta itemMeta = itemStack.getItemMeta();

        boolean hasCustomName = itemMeta != null && itemMeta.hasDisplayName();
        if (hasCustomName && Setting.ITEM_DISPLAY_CUSTOM_NAMES.getBoolean()) {
            if (Setting.ITEM_DISPLAY_CUSTOM_NAMES_COLOR.getBoolean()) {
                displayName = itemMeta.getDisplayName();
            } else {
                displayName = ChatColor.stripColor(itemMeta.getDisplayName());
            }
        } else {
            displayName = this.stackSettings.getDisplayName();
        }

        String displayString;
        if (this.getStackSize() > 1) {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("item-stack-display", StringPlaceholders.builder("amount", this.getStackSize())
                    .addPlaceholder("name", displayName).build());
        } else {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("item-stack-display-single", StringPlaceholders.single("name", displayName));
        }

        this.item.setCustomNameVisible(this.size > 1 || Setting.ITEM_DISPLAY_TAGS_SINGLE.getBoolean() || (Setting.ITEM_DISPLAY_CUSTOM_NAMES_ALWAYS.getBoolean() && hasCustomName));
        this.item.setCustomName(displayString);
    }

    /**
     * Gets the StackedItem that two stacks should stack into
     *
     * @param stack2 the second StackedItem
     * @return a positive int if this stack should be preferred, or a negative int if the other should be preferred
     */
    @Override
    public int compareTo(StackedItem stack2) {
        Entity entity1 = this.getItem();
        Entity entity2 = stack2.getItem();

        if (this == stack2)
            return 0;

        if (this.getStackSize() == stack2.getStackSize())
            return entity1.getTicksLived() > entity2.getTicksLived() ? 2 : -2;

        return this.getStackSize() > stack2.getStackSize() ? 1 : -1;
    }

}
