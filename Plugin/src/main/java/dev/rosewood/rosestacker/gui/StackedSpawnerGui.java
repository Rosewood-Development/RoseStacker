package dev.rosewood.rosestacker.gui;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.GuiString;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.SpawnerSpawnManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTag;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    public void openFor(Player player) {
        if (this.isInvalid())
            this.buildGui();
        this.guiContainer.openFor(player);
    }

    private void buildGui() {
        this.guiContainer = GuiFactory.createContainer()
                .setTickRate(Setting.SPAWNER_GUI_TICK_UPDATE_RATE.getInt());

        SpawnerStackSettings stackSettings = this.stackedSpawner.getStackSettings();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.localeManager.getLocaleMessage("gui-stacked-spawner-title", StringPlaceholders.single("name", stackSettings.getDisplayName())))
                .addButtonAt(11, GuiFactory.createButton())
                .addButtonAt(13, GuiFactory.createButton())
                .addButtonAt(15, GuiFactory.createButton());

        List<Integer> fillerSlots = IntStream.range(0, GuiSize.ROWS_THREE.getNumSlots()).boxed().collect(Collectors.toList());
        fillerSlots.removeAll(Arrays.asList(4, 7, 8, 9, 11, 13, 15, 17, 18, 19));

        Material filler = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_BORDER_MATERIAL.getString());
        Material corner = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_BORDER_CORNER_MATERIAL.getString());
        Material accent = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_BORDER_ACCENT_MATERIAL.getString());
        Material info = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_SPAWNER_STATS_MATERIAL.getString());
        Material spawner = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_CENTRAL_MATERIAL.getString());
        Material conditionsValid = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_VALID_SPAWN_CONDITIONS_MATERIAL.getString());
        Material conditionsInvalid = GuiHelper.parseMaterial(Setting.SPAWNER_GUI_INVALID_SPAWN_CONDITIONS_MATERIAL.getString());

        ItemStack fillerItem = new ItemStack(filler);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        if (fillerItemMeta != null) {
            fillerItemMeta.setDisplayName(" ");
            fillerItemMeta.addItemFlags(ItemFlag.values());
            fillerItem.setItemMeta(fillerItemMeta);
        }

        for (int slot : fillerSlots)
            mainScreen.addItemStackAt(slot, fillerItem);

        ItemStack cornerItem = fillerItem.clone();
        cornerItem.setType(corner);

        for (int slot : Arrays.asList(8, 18))
            mainScreen.addItemStackAt(slot, cornerItem);

        ItemStack accentItem = fillerItem.clone();
        accentItem.setType(accent);

        for (int slot : Arrays.asList(7, 9, 17, 19))
            mainScreen.addItemStackAt(slot, accentItem);

        ItemStack skull = RoseStacker.getInstance().getManager(StackSettingManager.class).getEntityStackSettings(stackSettings.getEntityType())
                .getEntityTypeData().getSkullItem();
        ItemMeta skullMeta = skull.getItemMeta();
        if (skullMeta != null) {
            String displayString;
            if (this.stackedSpawner.getStackSize() == 1 && !Setting.SPAWNER_DISPLAY_TAGS_SINGLE_AMOUNT.getBoolean()) {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-single", StringPlaceholders.builder("amount", this.stackedSpawner.getStackSize())
                        .addPlaceholder("name", this.stackedSpawner.getStackSettings().getDisplayName()).build());
            } else {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", this.stackedSpawner.getStackSize())
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

                    lore.add(this.getString("min-spawn-delay", StringPlaceholders.single("delay", stackSettings.getMinSpawnDelay())));
                    lore.add(this.getString("max-spawn-delay", StringPlaceholders.single("delay", stackSettings.getMaxSpawnDelay())));
                    lore.add(this.getString("disabled-mob-ai", StringPlaceholders.single("disabled", String.valueOf(stackSettings.isMobAIDisabled()))));
                    lore.add(this.getString("entity-search-range", StringPlaceholders.single("range", stackSettings.getEntitySearchRange())));
                    lore.add(this.getString("player-activation-range", StringPlaceholders.single("range", stackSettings.getPlayerActivationRange())));
                    lore.add(this.getString("spawn-range", StringPlaceholders.single("range", stackSettings.getSpawnRange())));

                    int spawnCount = this.stackedSpawner.getStackSize() * stackSettings.getSpawnCountStackSizeMultiplier();
                    if (Setting.SPAWNER_SPAWN_COUNT_STACK_SIZE_RANDOMIZED.getBoolean()) {
                        lore.add(this.getString("min-spawn-amount", StringPlaceholders.single("amount", this.stackedSpawner.getStackSize())));
                        lore.add(this.getString("max-spawn-amount", StringPlaceholders.single("amount", spawnCount)));
                    } else {
                        lore.add(this.getString("spawn-amount", StringPlaceholders.single("amount", spawnCount)));
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
                .setNameSupplier(() -> this.getString("time-until-next-spawn", StringPlaceholders.single("time", this.stackedSpawner.getLastDelay() - SpawnerSpawnManager.DELAY_THRESHOLD + 1)))
                .setLoreSupplier(() -> Collections.singletonList(this.getString("total-spawns", StringPlaceholders.single("amount", PersistentDataUtils.getTotalSpawnCount(this.stackedSpawner.getSpawner()))))
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
                        return Collections.singletonList(this.getString("entities-can-spawn"));

                    List<GuiString> lore = new ArrayList<>();
                    lore.add(this.getString("conditions-preventing-spawns"));

                    for (Class<? extends ConditionTag> conditionTagClass : invalidConditions)
                        lore.add(GuiFactory.createString(ConditionTags.getErrorMessage(conditionTagClass, this.localeManager)));

                    return lore;
                }));

        this.guiContainer.addScreen(mainScreen);

        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    private GuiString getString(String key, StringPlaceholders placeholders) {
        return GuiFactory.createString(this.localeManager.getLocaleMessage("gui-stacked-spawner-" + key, placeholders));
    }

    private GuiString getString(String key) {
        return this.getString(key, StringPlaceholders.empty());
    }

    public void kickOutViewers() {
        if (this.guiContainer != null)
            this.guiContainer.closeViewers();
    }

    private boolean isInvalid() {
        return this.guiContainer == null || !this.guiFramework.getGuiManager().getActiveGuis().contains(this.guiContainer);
    }

}
