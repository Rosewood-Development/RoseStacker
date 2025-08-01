package dev.rosewood.rosestacker.gui;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.GuiString;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTags;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StackedSpawnerGui {

    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n|\\\\n");
    private final LocaleManager localeManager;
    private final StackedSpawner stackedSpawner;
    private final GuiFramework guiFramework;
    private GuiContainer guiContainer;

    public StackedSpawnerGui(StackedSpawner stackedSpawner) {
        RosePlugin rosePlugin = RoseStacker.getInstance();

        this.localeManager = rosePlugin.getManager(LocaleManager.class);
        this.stackedSpawner = stackedSpawner;
        this.guiFramework = GuiFramework.instantiate(rosePlugin);
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
    }

    /**
     * Builds the GUI from scratch
     */
    private void buildGui() {
        this.guiContainer = GuiFactory.createContainer()
                .setTickRate(SettingKey.SPAWNER_GUI_TICK_UPDATE_RATE.get());

        SpawnerStackSettings stackSettings = this.stackedSpawner.getStackSettings();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.localeManager.getLocaleMessage("gui-stacked-spawner-title", StringPlaceholders.of("name", stackSettings.getDisplayName())))
                .addButtonAt(11, GuiFactory.createButton())
                .addButtonAt(13, GuiFactory.createButton())
                .addButtonAt(15, GuiFactory.createButton());

        Material filler = GuiHelper.parseMaterial(SettingKey.SPAWNER_GUI_BORDER_MATERIAL.get());
        Material corner = GuiHelper.parseMaterial(SettingKey.SPAWNER_GUI_BORDER_CORNER_MATERIAL.get());
        Material accent = GuiHelper.parseMaterial(SettingKey.SPAWNER_GUI_BORDER_ACCENT_MATERIAL.get());

        ItemStack fillerItem = new ItemStack(filler);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        if (fillerItemMeta != null) {
            fillerItemMeta.setDisplayName(" ");
            if (NMSUtil.getVersionNumber() >= 21) {
                fillerItemMeta.setHideTooltip(true);
            } else {
                fillerItemMeta.addItemFlags(ItemFlag.values());
            }
            fillerItem.setItemMeta(fillerItemMeta);
        }

        GuiUtil.fillScreen(mainScreen, fillerItem);

        ItemStack cornerItem = fillerItem.clone();
        cornerItem.setType(corner);

        for (int slot : List.of(8, 18))
            mainScreen.addItemStackAt(slot, cornerItem);

        ItemStack accentItem = fillerItem.clone();
        accentItem.setType(accent);

        for (int slot : List.of(7, 9, 17, 19))
            mainScreen.addItemStackAt(slot, accentItem);

        this.stackedSpawner.getSpawnerTile().getSpawnerType().get().ifPresent(spawnerType -> {
            Material info = GuiHelper.parseMaterial(SettingKey.SPAWNER_GUI_SPAWNER_STATS_MATERIAL.get());
            Material spawner = GuiHelper.parseMaterial(SettingKey.SPAWNER_GUI_CENTRAL_MATERIAL.get());
            Material conditionsValid = GuiHelper.parseMaterial(SettingKey.SPAWNER_GUI_VALID_SPAWN_CONDITIONS_MATERIAL.get());
            Material conditionsInvalid = GuiHelper.parseMaterial(SettingKey.SPAWNER_GUI_INVALID_SPAWN_CONDITIONS_MATERIAL.get());

            ItemStack skull = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(spawnerType)
                    .getEntityTypeData().getSkullItem();

            String skullDisplayName;
            if (this.stackedSpawner.getStackSize() == 1 && !SettingKey.SPAWNER_DISPLAY_TAGS_SINGLE_AMOUNT.get()) {
                skullDisplayName = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-single", StringPlaceholders.builder("amount", StackerUtils.formatNumber(this.stackedSpawner.getStackSize()))
                        .add("name", this.stackedSpawner.getStackSettings().getDisplayName()).build());
            } else {
                skullDisplayName = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", StackerUtils.formatNumber(this.stackedSpawner.getStackSize()))
                        .add("name", this.stackedSpawner.getStackSettings().getDisplayName()).build());
            }

            mainScreen.addButtonAt(4, GuiFactory.createButton(skull).setName(skullDisplayName));
            mainScreen.addButtonAt(11, GuiFactory.createButton()
                    .setIcon(info)
                    .setName(this.getString("stats"))
                    .setLoreSupplier(() -> {
                        List<GuiString> lore = new ArrayList<>();

                        StackedSpawnerTile spawnerTile = this.stackedSpawner.getSpawnerTile();
                        this.getAndAccumulateString(lore, "min-spawn-delay", StringPlaceholders.of("delay", spawnerTile.getMinSpawnDelay()));
                        this.getAndAccumulateString(lore, "max-spawn-delay", StringPlaceholders.of("delay", spawnerTile.getMaxSpawnDelay()));
                        this.getAndAccumulateString(lore, "disabled-mob-ai", StringPlaceholders.of("disabled", String.valueOf(stackSettings.isMobAIDisabled())));
                        int range = stackSettings.getEntitySearchRange() == -1 ? spawnerTile.getSpawnRange() : stackSettings.getEntitySearchRange();
                        this.getAndAccumulateString(lore, "entity-search-range", StringPlaceholders.of("range", range));
                        if (!this.stackedSpawner.getStackSettings().hasUnlimitedPlayerActivationRange())
                            this.getAndAccumulateString(lore, "player-activation-range", StringPlaceholders.of("range", spawnerTile.getRequiredPlayerRange()));
                        this.getAndAccumulateString(lore, "spawn-range", StringPlaceholders.of("range", spawnerTile.getSpawnRange()));

                        if (stackSettings.getSpawnCountStackSizeMultiplier() != -1) {
                            if (SettingKey.SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED.get()) {
                                this.getAndAccumulateString(lore, "min-spawn-amount", StringPlaceholders.of("amount", StackerUtils.formatNumber(this.stackedSpawner.getStackSize())));
                                this.getAndAccumulateString(lore, "max-spawn-amount", StringPlaceholders.of("amount", StackerUtils.formatNumber(spawnerTile.getSpawnCount())));
                            } else {
                                this.getAndAccumulateString(lore, "spawn-amount", StringPlaceholders.of("amount", StackerUtils.formatNumber(spawnerTile.getSpawnCount())));
                            }
                        } else {
                            this.getAndAccumulateString(lore, "min-spawn-amount", StringPlaceholders.of("amount", 1));
                            this.getAndAccumulateString(lore, "max-spawn-amount", StringPlaceholders.of("amount", StackerUtils.formatNumber(spawnerTile.getSpawnCount())));
                        }

                        List<GuiString> secondaryLore = new ArrayList<>();

                        this.getAndAccumulateString(secondaryLore, "spawn-conditions", StringPlaceholders.empty());

                        List<ConditionTag> spawnConditions = stackSettings.getSpawnRequirements();
                        for (ConditionTag conditionTag : spawnConditions)
                            for (String line : conditionTag.getInfoMessage(this.localeManager))
                                this.accumulateString(secondaryLore, line);

                        if (!secondaryLore.isEmpty()) {
                            lore.add(GuiFactory.createString());
                            lore.addAll(secondaryLore);
                        }

                        return lore;
                    }));
            List<ItemFlag> flags;
            if (NMSUtil.getVersionNumber() > 21 || NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 5) {
                flags = new ArrayList<>();
                if (NMSUtil.isPaper()) {
                    flags.add(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                } else {
                    try {
                        flags.add(ItemFlag.valueOf("HIDE_BLOCK_ENTITY_DATA"));
                    } catch (Exception ignored) { }
                }
            } else {
                flags = Arrays.asList(ItemFlag.values());
            }
            mainScreen.addButtonAt(13, GuiFactory.createButton()
                    .setIcon(spawner)
                    .setItemFlags(flags.toArray(ItemFlag[]::new))
                    .setNameSupplier(() -> this.getString("time-until-next-spawn", StringPlaceholders.of("time", StackerUtils.formatNumber(this.stackedSpawner.getSpawnerTile().getDelay() + 1))))
                    .setLoreSupplier(() -> {
                        List<GuiString> lore = new ArrayList<>();
                        this.getAndAccumulateString(lore, "total-spawns", StringPlaceholders.of("amount", StackerUtils.formatNumber(PersistentDataUtils.getTotalSpawnCount(this.stackedSpawner.getSpawnerTile()))));
                        return lore;
                    }));
            mainScreen.addButtonAt(15, GuiFactory.createButton()
                    .setIconSupplier(() -> GuiFactory.createIcon(this.stackedSpawner.getLastInvalidConditions().isEmpty() ? conditionsValid : conditionsInvalid))
                    .setNameSupplier(() -> {
                        if (this.stackedSpawner.getLastInvalidConditions().isEmpty()) {
                            return this.getString("valid-spawn-conditions");
                        } else {
                            return this.getString("invalid-spawn-conditions");
                        }
                    })
                    .setLoreSupplier(() -> {
                        List<Class<? extends ConditionTag>> invalidConditions = this.stackedSpawner.getLastInvalidConditions();
                        List<GuiString> lore = new ArrayList<>();
                        if (invalidConditions.isEmpty()) {
                            this.getAndAccumulateString(lore, "entities-can-spawn", StringPlaceholders.empty());
                            return lore;
                        }

                        this.getAndAccumulateString(lore, "conditions-preventing-spawns", StringPlaceholders.empty());

                        for (Class<? extends ConditionTag> conditionTagClass : invalidConditions)
                            this.accumulateString(lore, ConditionTags.getErrorMessage(conditionTagClass, this.localeManager));

                        return lore;
                    }));
        });

        this.guiContainer.addScreen(mainScreen);

        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    private void getAndAccumulateString(List<GuiString> accumulator, String key, StringPlaceholders placeholders) {
        String value = this.localeManager.getLocaleMessage("gui-stacked-spawner-" + key, placeholders);
        this.accumulateString(accumulator, value);
    }

    private void accumulateString(List<GuiString> accumulator, String value) {
        String[] values = NEWLINE_PATTERN.split(value);
        for (String s : values)
            if (!s.isEmpty())
                accumulator.add(GuiFactory.createString(s));
    }

    /**
     * Gets a spawner GUI string for one of the condition properties
     *
     * @param key The key in the locale file
     * @param placeholders Placeholders to be replaced
     * @return the GuiString to be displayed on the ItemStack
     */
    private GuiString getString(String key, StringPlaceholders placeholders) {
        return GuiFactory.createString(this.localeManager.getLocaleMessage("gui-stacked-spawner-" + key, placeholders));
    }

    /**
     * Gets a spawner GUI string for one of the condition properties
     *
     * @param key The key in the locale file
     * @return the GuiString to be displayed on the ItemStack
     */
    private GuiString getString(String key) {
        return this.getString(key, StringPlaceholders.empty());
    }

    /**
     * Forcefully closes the GUI for all viewers
     */
    public void kickOutViewers() {
        if (this.guiContainer != null)
            this.guiContainer.closeViewers();
    }

    /**
     * @return true if the GUI needs to be rebuilt, false otherwise
     */
    private boolean isInvalid() {
        return this.guiContainer == null || !this.guiFramework.getGuiManager().getActiveGuis().contains(this.guiContainer);
    }

}
