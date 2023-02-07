package dev.rosewood.rosestacker.gui;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.GuiString;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.conditions.spawner.ConditionTags;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StackedSpawnerGui {

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
                .setTickRate(Setting.SPAWNER_GUI_TICK_UPDATE_RATE.getInt());

        SpawnerStackSettings stackSettings = this.stackedSpawner.getStackSettings();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.localeManager.getLocaleMessage("gui-stacked-spawner-title", StringPlaceholders.single("name", stackSettings.getDisplayName())))
                .addButtonAt(11, GuiFactory.createButton())
                .addButtonAt(13, GuiFactory.createButton())
                .addButtonAt(15, GuiFactory.createButton());

        Material filler = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_BORDER_MATERIAL.getString());
        Material corner = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_BORDER_CORNER_MATERIAL.getString());
        Material accent = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_BORDER_ACCENT_MATERIAL.getString());

        ItemStack fillerItem = new ItemStack(filler);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        if (fillerItemMeta != null) {
            fillerItemMeta.setDisplayName(" ");
            fillerItemMeta.addItemFlags(ItemFlag.values());
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
            Material info = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_SPAWNER_STATS_MATERIAL.getString());
            Material spawner = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_CENTRAL_MATERIAL.getString());
            Material conditionsValid = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_VALID_SPAWN_CONDITIONS_MATERIAL.getString());
            Material conditionsInvalid = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_INVALID_SPAWN_CONDITIONS_MATERIAL.getString());

            ItemStack skull = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(spawnerType)
                    .getEntityTypeData().getSkullItem();
            ItemMeta skullMeta = skull.getItemMeta();
            if (skullMeta != null) {
                String displayString;
                if (this.stackedSpawner.getStackSize() == 1 && !Setting.SPAWNER_DISPLAY_TAGS_SINGLE_AMOUNT.getBoolean()) {
                    displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-single", StringPlaceholders.builder("amount", StackerUtils.formatNumber(this.stackedSpawner.getStackSize()))
                            .addPlaceholder("name", this.stackedSpawner.getStackSettings().getDisplayName()).build());
                } else {
                    displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", StackerUtils.formatNumber(this.stackedSpawner.getStackSize()))
                            .addPlaceholder("name", this.stackedSpawner.getStackSettings().getDisplayName()).build());
                }
                skullMeta.setDisplayName(displayString);
                skull.setItemMeta(skullMeta);
            }

            mainScreen.addButtonAt(4, GuiFactory.createButton(skull));
            mainScreen.addButtonAt(11, GuiFactory.createButton()
                    .setIcon(info)
                    .setName(this.getString("stats"))
                    .setLoreSupplier(() -> {
                        List<GuiString> lore = new ArrayList<>();

                        StackedSpawnerTile spawnerTile = this.stackedSpawner.getSpawnerTile();
                        lore.add(this.getString("min-spawn-delay", StringPlaceholders.single("delay", spawnerTile.getMinSpawnDelay())));
                        lore.add(this.getString("max-spawn-delay", StringPlaceholders.single("delay", spawnerTile.getMaxSpawnDelay())));
                        lore.add(this.getString("disabled-mob-ai", StringPlaceholders.single("disabled", String.valueOf(stackSettings.isMobAIDisabled()))));
                        int range = stackSettings.getEntitySearchRange() == -1 ? spawnerTile.getSpawnRange() : stackSettings.getEntitySearchRange();
                        lore.add(this.getString("entity-search-range", StringPlaceholders.single("range", range)));
                        if (!this.stackedSpawner.getStackSettings().hasUnlimitedPlayerActivationRange())
                            lore.add(this.getString("player-activation-range", StringPlaceholders.single("range", spawnerTile.getRequiredPlayerRange())));
                        lore.add(this.getString("spawn-range", StringPlaceholders.single("range", spawnerTile.getSpawnRange())));

                        if (stackSettings.getSpawnCountStackSizeMultiplier() != -1) {
                            if (Setting.SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED.getBoolean()) {
                                lore.add(this.getString("min-spawn-amount", StringPlaceholders.single("amount", StackerUtils.formatNumber(this.stackedSpawner.getStackSize()))));
                                lore.add(this.getString("max-spawn-amount", StringPlaceholders.single("amount", StackerUtils.formatNumber(spawnerTile.getSpawnCount()))));
                            } else {
                                lore.add(this.getString("spawn-amount", StringPlaceholders.single("amount", StackerUtils.formatNumber(spawnerTile.getSpawnCount()))));
                            }
                        } else {
                            lore.add(this.getString("min-spawn-amount", StringPlaceholders.single("amount", 1)));
                            lore.add(this.getString("max-spawn-amount", StringPlaceholders.single("amount", StackerUtils.formatNumber(spawnerTile.getSpawnCount()))));
                        }

                        lore.add(GuiFactory.createString());
                        lore.add(this.getString("spawn-conditions"));

                        List<ConditionTag> spawnConditions = stackSettings.getSpawnRequirements();
                        for (ConditionTag conditionTag : spawnConditions)
                            for (String line : conditionTag.getInfoMessage(this.localeManager))
                                lore.add(GuiFactory.createString(line));

                        return lore;
                    }));
            mainScreen.addButtonAt(13, GuiFactory.createButton()
                    .setIcon(spawner)
                    .setNameSupplier(() -> this.getString("time-until-next-spawn", StringPlaceholders.single("time", StackerUtils.formatNumber(this.stackedSpawner.getSpawnerTile().getDelay() + 1))))
                    .setLoreSupplier(() -> List.of(this.getString("total-spawns", StringPlaceholders.single("amount", StackerUtils.formatNumber(PersistentDataUtils.getTotalSpawnCount(this.stackedSpawner.getSpawnerTile())))))
                    ));
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
                        if (invalidConditions.isEmpty())
                            return List.of(this.getString("entities-can-spawn"));

                        List<GuiString> lore = new ArrayList<>();
                        lore.add(this.getString("conditions-preventing-spawns"));

                        for (Class<? extends ConditionTag> conditionTagClass : invalidConditions)
                            lore.add(GuiFactory.createString(ConditionTags.getErrorMessage(conditionTagClass, this.localeManager)));

                        return lore;
                    }));
        });

        this.guiContainer.addScreen(mainScreen);

        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
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
