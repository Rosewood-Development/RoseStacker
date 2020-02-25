package dev.esophose.rosestacker.gui;

import dev.esophose.guiframework.GuiFramework;
import dev.esophose.guiframework.gui.ClickAction;
import dev.esophose.guiframework.gui.GuiButton;
import dev.esophose.guiframework.gui.GuiButtonFlag;
import dev.esophose.guiframework.gui.GuiContainer;
import dev.esophose.guiframework.gui.GuiSize;
import dev.esophose.guiframework.gui.screen.GuiScreen;
import dev.esophose.guiframework.gui.screen.GuiScreenSection;
import dev.esophose.guiframework.util.GuiUtil;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.manager.LocaleManager;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.manager.StackSettingManager;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.settings.BlockStackSettings;
import dev.esophose.rosestacker.utils.StackerUtils;
import dev.esophose.rosestacker.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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
        this.guiContainer = new GuiContainer();

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
        ItemStack borderItem = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta itemMeta = borderItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("");
            borderItem.setItemMeta(itemMeta);
        }

        List<Integer> destroyBorderSlots = new ArrayList<>();
        for (int i = 0; i <= 26; i++) destroyBorderSlots.add(i);
        destroyBorderSlots.removeAll(Arrays.asList(12, 14));

        GuiScreenSection editableSection = new GuiScreenSection(paginatedSlots);
        LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);
        BlockStackSettings stackSettings = this.roseStacker.getManager(StackSettingManager.class).getBlockStackSettings(this.stackedBlock.getBlock());

        GuiStringHelper pageBackString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-page-back", StringPlaceholders.empty()));
        GuiStringHelper destroyString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-destroy", StringPlaceholders.empty()));
        GuiStringHelper pageForwardString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-page-forward", StringPlaceholders.empty()));
        GuiStringHelper confirmDestroyString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-destroy-confirm", StringPlaceholders.empty()));
        GuiStringHelper confirmCancelString = new GuiStringHelper(localeManager.getGuiLocaleMessage("gui-stacked-block-destroy-cancel", StringPlaceholders.empty()));

        List<ItemStack> stackItems = GuiUtil.getMaterialAmountAsItemStacks(this.stackedBlock.getBlock().getType(), this.stackedBlock.getStackSize());
        int pages = (int) Math.ceil((double) stackItems.size() / paginatedSlots.size()) + 1;
        while (stackItems.size() < pages * paginatedSlots.size())
            stackItems.add(new ItemStack(Material.AIR));

        GuiScreen mainScreen = new GuiScreen(this.guiContainer, GuiSize.ROWS_SIX)
                .setTitle(localeManager.getLocaleMessage("gui-stacked-block-title", StringPlaceholders.single("name", stackSettings.getDisplayName())))
                .setEditableSection(editableSection, stackItems, this::updateStackedBlock)
                .addButtonAt(47, new GuiButton()
                        .setIcon(Material.PAPER)
                        .setName(pageBackString.getName())
                        .setLore(pageBackString.getLore())
                        .setClickAction(event -> ClickAction.PAGE_BACKWARDS)
                        .setFlags(GuiButtonFlag.HIDE_IF_FIRST_PAGE)
                        .setHiddenReplacement(borderItem))
                .addButtonAt(49, new GuiButton()
                        .setIcon(Material.BARRIER)
                        .setName(destroyString.getName())
                        .setLore(destroyString.getLore())
                        .setClickAction(event -> ClickAction.TRANSITION_FORWARDS))
                .addButtonAt(51, new GuiButton()
                        .setIcon(Material.PAPER)
                        .setName(pageForwardString.getName())
                        .setLore(pageForwardString.getLore())
                        .setClickAction(event -> ClickAction.PAGE_FORWARDS)
                        .setFlags(GuiButtonFlag.HIDE_IF_LAST_PAGE)
                        .setHiddenReplacement(borderItem));

        for (int slot : borderSlots)
            mainScreen.addItemStackAt(slot, borderItem);

        GuiScreen confirmScreen = new GuiScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(localeManager.getLocaleMessage("gui-stacked-block-destroy-title", StringPlaceholders.single("name", stackSettings.getDisplayName())))
                .addButtonAt(12, new GuiButton()
                        .setIcon(Material.EMERALD_BLOCK)
                        .setName(confirmDestroyString.getName())
                        .setLore(confirmDestroyString.getLore())
                        .setClickAction(event -> {
                            this.destroyStackedBlock();
                            return ClickAction.NOTHING;
                        }))
                .addButtonAt(14, new GuiButton()
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

        int newStackSize = items.stream().mapToInt(ItemStack::getAmount).sum();
        this.stackedBlock.setStackSize(newStackSize);

        int maxStackSize = Setting.BLOCK_MAX_STACK_SIZE.getInt();
        if (newStackSize == 1) {
            stackManager.removeBlockStack(this.stackedBlock);
        } else if (newStackSize == 0) {
            stackManager.removeBlockStack(this.stackedBlock);
        } else if (newStackSize > maxStackSize) {
            List<ItemStack> overflowItems = GuiUtil.getMaterialAmountAsItemStacks(this.stackedBlock.getBlock().getType(), newStackSize - maxStackSize);
            StackerUtils.dropItemsToPlayer(player, overflowItems);
            this.stackedBlock.setStackSize(maxStackSize);
        }

        this.guiContainer = null;
    }

    private void destroyStackedBlock() {
        this.kickOutViewers();

        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        Bukkit.getScheduler().runTask(this.roseStacker, () -> {
            List<ItemStack> itemsToDrop = GuiUtil.getMaterialAmountAsItemStacks(this.stackedBlock.getBlock().getType(), this.stackedBlock.getStackSize() - 1);
            Location dropLocation = this.stackedBlock.getLocation().clone();
            dropLocation.add(0.5, 0.5, 0.5);

            ItemStack silkTouchItem = new ItemStack(Material.DIAMOND_PICKAXE);
            silkTouchItem.addEnchantment(Enchantment.SILK_TOUCH, 1);

            this.stackedBlock.setStackSize(0);
            this.stackedBlock.getBlock().breakNaturally(silkTouchItem);
            stackManager.preStackItems(itemsToDrop, dropLocation);

            stackManager.removeBlockStack(this.stackedBlock);
        });
    }

    private boolean isInvalid() {
        return this.guiContainer == null || !this.guiFramework.getGuiManager().getActiveGuis().contains(this.guiContainer);
    }

}
