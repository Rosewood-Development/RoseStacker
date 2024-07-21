package dev.rosewood.rosestacker.gui;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.framework.gui.FrameworkView;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiButtonFlag;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.guiframework.gui.screen.GuiScreenSection;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.event.BlockStackEvent;
import dev.rosewood.rosestacker.event.BlockUnstackEvent;
import dev.rosewood.rosestacker.gui.GuiHelper.GuiStringHelper;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class StackedBlockGui {

    private final RosePlugin rosePlugin;
    private final StackedBlock stackedBlock;

    private final GuiFramework guiFramework;
    private GuiContainer guiContainer;
    private Material stackType;

    public StackedBlockGui(StackedBlock stackedBlock) {
        this.rosePlugin = RoseStacker.getInstance();
        this.stackedBlock = stackedBlock;

        this.guiFramework = GuiFramework.instantiate(this.rosePlugin);
        this.guiContainer = null;
    }

    /**
     * Opens the GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public void openFor(Player player) {
        if (this.isInvalid())
            this.buildGui();

        this.guiContainer.openFor(player);
        // TODO: Add support for switching the currently viewed page directly to GuiFramework instead, this works for now
        FrameworkView view = (FrameworkView) this.guiContainer.getCurrentViewers().get(player.getUniqueId());
        if (view != null) {
            view.setViewingPage(view.getViewingScreen().getMaximumPageNumber() - 1);
            player.openInventory(view.getViewingScreen().getInventory(view.getViewingPage()));
        }
    }

    /**
     * Builds the GUI from scratch
     */
    private void buildGui() {
        this.stackType = this.stackedBlock.getBlock().getType();
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
        borderSlots.addAll(List.of(45, 53));
        ItemStack borderItem = new ItemStack(GuiHelper.parseMaterial(SettingKey.BLOCK_GUI_BORDER_MATERIAL.get()));
        ItemMeta itemMeta = borderItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            itemMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(itemMeta);
        }

        List<Integer> destroyBorderSlots = new ArrayList<>();
        for (int i = 0; i <= 26; i++) destroyBorderSlots.add(i);
        destroyBorderSlots.removeAll(List.of(12, 14));

        GuiScreenSection editableSection = GuiFactory.createScreenSection(paginatedSlots);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
        BlockStackSettings stackSettings = this.stackedBlock.getStackSettings();

        GuiStringHelper pageBackString = new GuiStringHelper(localeManager.getLocaleMessages("gui-stacked-block-page-back", StringPlaceholders.empty()));
        GuiStringHelper destroyString = new GuiStringHelper(localeManager.getLocaleMessages("gui-stacked-block-destroy", StringPlaceholders.empty()));
        GuiStringHelper pageForwardString = new GuiStringHelper(localeManager.getLocaleMessages("gui-stacked-block-page-forward", StringPlaceholders.empty()));
        GuiStringHelper confirmDestroyString = new GuiStringHelper(localeManager.getLocaleMessages("gui-stacked-block-destroy-confirm", StringPlaceholders.empty()));
        GuiStringHelper confirmCancelString = new GuiStringHelper(localeManager.getLocaleMessages("gui-stacked-block-destroy-cancel", StringPlaceholders.empty()));

        List<ItemStack> stackItems = GuiUtil.getMaterialAmountAsItemStacks(this.stackType, this.stackedBlock.getStackSize());
        int pages = (int) Math.ceil((double) stackItems.size() / paginatedSlots.size()) + 1;
        while (stackItems.size() < pages * paginatedSlots.size())
            stackItems.add(new ItemStack(Material.AIR));

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_SIX)
                .setTitle(localeManager.getLocaleMessage("gui-stacked-block-title", StringPlaceholders.of("name", stackSettings.getDisplayName())))
                .setEditableSection(editableSection, stackItems, this::updateStackedBlock)
                .setEditFilters(GuiFactory.createScreenEditFilters()
                        .setWhitelist(this.stackType)
                        .setAllowModified(false))
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
                .setTitle(localeManager.getLocaleMessage("gui-stacked-block-destroy-title", StringPlaceholders.of("name", stackSettings.getDisplayName())))
                .addButtonAt(12, GuiFactory.createButton()
                        .setIcon(Material.EMERALD_BLOCK)
                        .setName(confirmDestroyString.getName())
                        .setLore(confirmDestroyString.getLore())
                        .setClickAction(event -> {
                            this.destroyStackedBlock((Player) event.getWhoClicked());
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

    /**
     * Forcefully closes the GUI for all viewers
     */
    public void kickOutViewers() {
        if (this.guiContainer != null)
            this.guiContainer.closeViewers();
    }

    /**
     * @return true if this GUI has any viewers, false otherwise
     */
    public boolean hasViewers() {
        return this.guiContainer != null;
    }

    /**
     * Updates the StackedBlock with the changed item contents
     *
     * @param player The Player that finalized the change
     * @param items The Items that are inside the StackedBlock
     */
    private void updateStackedBlock(Player player, List<ItemStack> items) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);

        // No longer any players viewing
        this.guiContainer = null;

        int newStackSize = items.stream()
                .filter(Objects::nonNull)
                .mapToInt(ItemStack::getAmount).sum();
        if (newStackSize == this.stackedBlock.getStackSize())
            return;

        int difference = newStackSize - this.stackedBlock.getStackSize();
        if (newStackSize > this.stackedBlock.getStackSize()) {
            BlockStackEvent blockStackEvent = new BlockStackEvent(player, this.stackedBlock, difference, false);
            Bukkit.getPluginManager().callEvent(blockStackEvent);
            if (blockStackEvent.isCancelled()) {
                ItemUtils.dropItemsToPlayer(player, GuiUtil.getMaterialAmountAsItemStacks(this.stackType, difference));
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
            List<ItemStack> overflowItems = GuiUtil.getMaterialAmountAsItemStacks(this.stackType, newStackSize - maxStackSize);
            ItemUtils.dropItemsToPlayer(player, overflowItems);
            this.stackedBlock.setStackSize(maxStackSize);
        }
    }

    /**
     * Takes away items from a Player
     *
     * @param player The Player to take items from
     * @param amount The number of items to take
     */
    private void takeFromPlayer(Player player, int amount) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] contents = playerInventory.getContents();

        for (ItemStack itemStack : contents) {
            if (itemStack == null || itemStack.getType() != this.stackType)
                continue;

            int amountToTake = Math.min(amount, itemStack.getAmount());
            itemStack.setAmount(itemStack.getAmount() - amountToTake);
            amount -= amountToTake;

            if (amount == 0)
                break;
        }

        playerInventory.setContents(contents);
    }

    /**
     * Destroys the StackedBlock and drops all its items either onto the ground or a player inventory
     *
     * @param player The Player that destroyed the stack
     */
    private void destroyStackedBlock(Player player) {
        this.kickOutViewers();

        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        ThreadUtils.runSync(() -> {
            BlockUnstackEvent blockUnstackEvent = new BlockUnstackEvent(player, this.stackedBlock, this.stackedBlock.getStackSize());
            Bukkit.getPluginManager().callEvent(blockUnstackEvent);
            if (blockUnstackEvent.isCancelled())
                return;

            List<ItemStack> itemsToDrop;
            if (SettingKey.BLOCK_BREAK_ENTIRE_STACK_INTO_SEPARATE.get()) {
                itemsToDrop = GuiUtil.getMaterialAmountAsItemStacks(this.stackType, blockUnstackEvent.getDecreaseAmount());
            } else {
                itemsToDrop = List.of(ItemUtils.getBlockAsStackedItemStack(this.stackType, blockUnstackEvent.getDecreaseAmount()));
            }

            if (SettingKey.BLOCK_DROP_TO_INVENTORY.get()) {
                ItemUtils.dropItemsToPlayer(player, itemsToDrop);
            } else {
                stackManager.preStackItems(itemsToDrop, this.stackedBlock.getLocation());
            }

            this.stackedBlock.setStackSize(this.stackedBlock.getStackSize() - blockUnstackEvent.getDecreaseAmount());
            if (this.stackedBlock.getStackSize() <= 0) {
                stackManager.removeBlockStack(this.stackedBlock);
                this.stackedBlock.getBlock().setType(Material.AIR);

                this.stackedBlock.getBlock().getWorld().playSound(this.stackedBlock.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 0.01F);
            }
        });
    }

    /**
     * @return true if the GUI needs to be rebuilt, false otherwise
     */
    private boolean isInvalid() {
        return this.guiContainer == null || !this.guiFramework.getGuiManager().getActiveGuis().contains(this.guiContainer);
    }

}
