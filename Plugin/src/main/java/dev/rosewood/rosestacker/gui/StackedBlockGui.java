package dev.rosewood.rosestacker.gui;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiButtonFlag;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.guiframework.gui.screen.GuiScreenSection;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.event.BlockStackEvent;
import dev.rosewood.rosestacker.event.BlockUnstackEvent;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StackedBlockGui {

    private final RoseStacker roseStacker;
    private final StackedBlock stackedBlock;

    private final GuiFramework guiFramework;
    private GuiContainer guiContainer;

    public StackedBlockGui(StackedBlock stackedBlock) {
        this.roseStacker = RoseStacker.getInstance();
        this.stackedBlock = stackedBlock;

        this.guiFramework = GuiFramework.instantiate(this.roseStacker);
        this.guiContainer = null;
    }

    public void openFor(Player player) {
        if (this.isInvalid())
            this.buildGui();
        this.guiContainer.openFor(player);
    }

    private void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        List<Integer> paginatedSlots = new ArrayList<>();
        for (int i = 10; i <= 16; i++) paginatedSlots.add(i);
        for (int i = 19; i <= 25; i++) paginatedSlots.add(i);
        for (int i = 28; i <= 34; i++) paginatedSlots.add(i);
        for (int i = 37; i <= 43; i++) paginatedSlots.add(i);

        List<Integer> borderSlots = new ArrayList<>();
        for (int i = 0; i <= 8; i++) borderSlots.add(i);
        for (int i = 9; i <= 36; i += 9) borderSlots.add(i);
        for (int i = 17; i <= 44; i += 9) borderSlots.add(i);
        for (int i = 46; i <= 52; i += 2) borderSlots.add(i);
        borderSlots.addAll(Arrays.asList(45, 53));
        ItemStack borderItem = new ItemStack(Material.valueOf(Setting.BLOCK_GUI_BORDER_MATERIAL.getString()));
        ItemMeta itemMeta = borderItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            itemMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(itemMeta);
        }

        List<Integer> destroyBorderSlots = new ArrayList<>();
        for (int i = 0; i <= 26; i++) destroyBorderSlots.add(i);
        destroyBorderSlots.removeAll(Arrays.asList(12, 14));

        GuiScreenSection editableSection = GuiFactory.createScreenSection(paginatedSlots);
        LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);
        BlockStackSettings stackSettings = this.stackedBlock.getStackSettings();

        GuiStringHelper pageBackString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-page-back", StringPlaceholders.empty()));
        GuiStringHelper destroyString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-destroy", StringPlaceholders.empty()));
        GuiStringHelper pageForwardString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-page-forward", StringPlaceholders.empty()));
        GuiStringHelper confirmDestroyString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-destroy-confirm", StringPlaceholders.empty()));
        GuiStringHelper confirmCancelString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-destroy-cancel", StringPlaceholders.empty()));

        List<ItemStack> stackItems = GuiUtil.getMaterialAmountAsItemStacks(this.stackedBlock.getBlock().getType(), this.stackedBlock.getStackSize());
        int pages = (int) Math.ceil((double) stackItems.size() / paginatedSlots.size()) + 1;
        while (stackItems.size() < pages * paginatedSlots.size())
            stackItems.add(new ItemStack(Material.AIR));

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_SIX)
                .setTitle(localeManager.getLocaleMessage("gui-stacked-block-title", StringPlaceholders.single("name", stackSettings.getDisplayName())))
                .setEditableSection(editableSection, stackItems, this::updateStackedBlock)
                .setEditFilters(GuiFactory.createScreenEditFilters()
                        .setWhitelist(this.stackedBlock.getBlock().getType()))
                .addButtonAt(47, GuiFactory.createButton()
                        .setIcon(Material.PAPER)
                        .setName(pageBackString.getName())
                        .setLore(pageBackString.getLore())
                        .setClickAction(event -> ClickAction.PAGE_BACKWARDS)
                        .setFlags(GuiButtonFlag.HIDE_IF_FIRST_PAGE)
                        .setHiddenReplacement(borderItem))
                .addButtonAt(49, GuiFactory.createButton()
                        .setIcon(Material.BARRIER)
                        .setName(destroyString.getName())
                        .setLore(destroyString.getLore())
                        .setClickAction(event -> ClickAction.TRANSITION_FORWARDS))
                .addButtonAt(51, GuiFactory.createButton()
                        .setIcon(Material.PAPER)
                        .setName(pageForwardString.getName())
                        .setLore(pageForwardString.getLore())
                        .setClickAction(event -> ClickAction.PAGE_FORWARDS)
                        .setFlags(GuiButtonFlag.HIDE_IF_LAST_PAGE)
                        .setHiddenReplacement(borderItem));

        for (int slot : borderSlots)
            mainScreen.addItemStackAt(slot, borderItem);

        GuiScreen confirmScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(localeManager.getLocaleMessage("gui-stacked-block-destroy-title", StringPlaceholders.single("name", stackSettings.getDisplayName())))
                .addButtonAt(12, GuiFactory.createButton()
                        .setIcon(Material.EMERALD_BLOCK)
                        .setName(confirmDestroyString.getName())
                        .setLore(confirmDestroyString.getLore())
                        .setClickAction(event -> {
                            this.destroyStackedBlock();
                            return ClickAction.NOTHING;
                        }))
                .addButtonAt(14, GuiFactory.createButton()
                        .setIcon(Material.REDSTONE_BLOCK)
                        .setName(confirmCancelString.getName())
                        .setLore(confirmCancelString.getLore())
                        .setClickAction(event -> ClickAction.TRANSITION_BACKWARDS));

        for (int slot : destroyBorderSlots)
            confirmScreen.addItemStackAt(slot, borderItem);

        this.guiContainer.addScreen(mainScreen);
        this.guiContainer.addScreen(confirmScreen);

        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    public void kickOutViewers() {
        if (this.guiContainer != null)
            this.guiContainer.closeViewers();
    }

    public boolean hasViewers() {
        return this.guiContainer != null;
    }

    private void updateStackedBlock(Player player, List<ItemStack> items) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);

        // No longer any players viewing
        this.guiContainer = null;

        int newStackSize = items.stream().mapToInt(ItemStack::getAmount).sum();
        if (newStackSize == this.stackedBlock.getStackSize())
            return;

        int difference = newStackSize - this.stackedBlock.getStackSize();
        if (newStackSize > this.stackedBlock.getStackSize()) {
            BlockStackEvent blockStackEvent = new BlockStackEvent(player, this.stackedBlock, difference);
            Bukkit.getPluginManager().callEvent(blockStackEvent);
            if (blockStackEvent.isCancelled()) {
                StackerUtils.dropItemsToPlayer(player, GuiUtil.getMaterialAmountAsItemStacks(this.stackedBlock.getBlock().getType(), difference));
                return;
            }

            newStackSize = this.stackedBlock.getStackSize() + blockStackEvent.getIncreaseAmount();
        } else {
            BlockUnstackEvent blockUnstackEvent = new BlockUnstackEvent(player, this.stackedBlock, -difference);
            Bukkit.getPluginManager().callEvent(blockUnstackEvent);
            if (blockUnstackEvent.isCancelled()) {
                this.takeFromPlayer(player, -difference);
                return;
            }

            newStackSize = this.stackedBlock.getStackSize() - blockUnstackEvent.getDecreaseAmount();
        }

        this.stackedBlock.setStackSize(newStackSize);

        int maxStackSize = this.stackedBlock.getStackSettings().getMaxStackSize();
        if (newStackSize == 1) {
            stackManager.removeBlockStack(this.stackedBlock);
        } else if (newStackSize == 0) {
            stackManager.removeBlockStack(this.stackedBlock);
            this.stackedBlock.getBlock().setType(Material.AIR);
        } else if (newStackSize > maxStackSize) {
            List<ItemStack> overflowItems = GuiUtil.getMaterialAmountAsItemStacks(this.stackedBlock.getBlock().getType(), newStackSize - maxStackSize);
            StackerUtils.dropItemsToPlayer(player, overflowItems);
            this.stackedBlock.setStackSize(maxStackSize);
        }
    }

    private void takeFromPlayer(Player player, int amount) {
        int toRemove = amount;
        Inventory playerInventory = player.getInventory();
        int slot = 0;
        while (toRemove > 0 && slot < playerInventory.getSize()) {
            ItemStack item = playerInventory.getItem(slot);
            if (item == null || item.getType() != this.stackedBlock.getBlock().getType())
                continue;

            if (toRemove >= item.getAmount()) {
                toRemove -= item.getAmount();
                playerInventory.setItem(slot, null);
            } else {
                toRemove = 0;
                item.setAmount(item.getAmount() - toRemove);
            }
        }
    }

    private void destroyStackedBlock() {
        this.kickOutViewers();

        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        Bukkit.getScheduler().runTask(this.roseStacker, () -> {
            List<ItemStack> itemsToDrop = GuiUtil.getMaterialAmountAsItemStacks(this.stackedBlock.getBlock().getType(), this.stackedBlock.getStackSize());
            Location dropLocation = this.stackedBlock.getLocation().clone();
            dropLocation.add(0.5, 0.5, 0.5);

            this.stackedBlock.setStackSize(0);
            stackManager.preStackItems(itemsToDrop, dropLocation);

            stackManager.removeBlockStack(this.stackedBlock);
            this.stackedBlock.getBlock().setType(Material.AIR);
        });
    }

    private boolean isInvalid() {
        return this.guiContainer == null || !this.guiFramework.getGuiManager().getActiveGuis().contains(this.guiContainer);
    }

}
