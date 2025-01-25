package dev.rosewood.rosestacker.stack;

import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.stack.settings.ItemStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StackedItem extends Stack<ItemStackSettings> implements Comparable<StackedItem> {

    private int size;
    private Item item;

    private ItemStackSettings stackSettings;
    private double x, y, z;

    public StackedItem(int size, Item item, boolean updateDisplay) {
        this.size = size;
        this.item = item;

        if (this.item != null) {
            this.stackSettings = RoseStacker.getInstance().getManager(StackSettingManager.class).getItemStackSettings(this.item);

            if (updateDisplay)
                this.updateDisplay();
        }
    }

    public StackedItem(int size, Item item) {
        this(size, item, true);
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

    public void increaseStackSize(int amount, boolean updateDisplay) {
        this.size += amount;
        if (updateDisplay)
            this.updateDisplay();
    }

    public void setStackSize(int size) {
        this.size = size;
        this.updateDisplay();
    }

    public int getAge() {
        return this.item.getTicksLived();
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

        if (itemStack.getType() == Material.AIR)
            return;

        this.item.setItemStack(itemStack);

        if (this.stackSettings == null || !this.stackSettings.isStackingEnabled() || !this.stackSettings.shouldDisplayTags()) {
            this.item.setCustomNameVisible(false);
            return;
        }

        String displayName = null;
        boolean hasCustomName = false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta.hasDisplayName() && SettingKey.ITEM_DISPLAY_CUSTOM_NAMES.get()) {
                displayName = itemMeta.getDisplayName();
                hasCustomName = true;
            } else if (NMSUtil.getVersionNumber() >= 21 && itemMeta.hasItemName()) { // Support item_name component in 1.21+
                displayName = itemMeta.getItemName();
                hasCustomName = true;
            }
        }

        if (displayName != null && !SettingKey.ITEM_DISPLAY_CUSTOM_NAMES_COLOR.get())
            displayName = ChatColor.stripColor(displayName);

        if (displayName == null)
            displayName = this.stackSettings.getDisplayName();

        StringPlaceholders.Builder placeholdersBuilder = StringPlaceholders.builder()
                .add("amount", StackerUtils.formatNumber(this.getStackSize()))
                .add("name", displayName);

        if (SettingKey.ITEM_DISPLAY_DESPAWN_TIMER_PLACEHOLDER.get()) {
            String timer;
            if (NMSUtil.getVersionNumber() >= 18 && this.item.isUnlimitedLifetime()) {
                timer = "∞";
            } else {
                int despawnRate = NMSAdapter.getHandler().getItemDespawnRate(this.item);
                int ticksLeft = despawnRate - this.getAge();
                int secondsLeft = ticksLeft / 20;
                timer = String.format("%d:%02d", secondsLeft / 60, secondsLeft % 60);
            }
            placeholdersBuilder.add("timer", timer);
        }

        String displayString;
        if (this.getStackSize() > 1) {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("item-stack-display", placeholdersBuilder.build());
        } else {
            displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("item-stack-display-single", placeholdersBuilder.build());
        }

        this.item.setCustomNameVisible((this.size > 1 || SettingKey.ITEM_DISPLAY_TAGS_SINGLE.get() || (SettingKey.ITEM_DISPLAY_CUSTOM_NAMES_ALWAYS.get() && hasCustomName)) &&
                (this.size > itemStack.getMaxStackSize() || !SettingKey.ITEM_DISPLAY_TAGS_ABOVE_VANILLA_STACK_SIZE.get()));
        NMSAdapter.getHandler().setCustomNameUncapped(this.item, displayString);
    }

    @Override
    public ItemStackSettings getStackSettings() {
        return this.stackSettings;
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

        if (SettingKey.ITEM_MERGE_INTO_NEWEST.get())
            return entity1.getTicksLived() < entity2.getTicksLived() ? 1 : -1;

        if (this.getStackSize() == stack2.getStackSize())
            return entity1.getTicksLived() > entity2.getTicksLived() ? 2 : -2;

        return this.getStackSize() > stack2.getStackSize() ? 1 : -1;
    }

    /**
     * @return true if the entity has moved since the last time this method was called, false otherwise
     */
    public boolean hasMoved() {
        Location location = this.item.getLocation();
        boolean moved = location.getX() != this.x || location.getY() != this.y || location.getZ() != this.z;
        if (moved) {
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
        }
        return moved;
    }

}
