package dev.rosewood.rosestacker.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.manager.ConversionManager;
import dev.rosewood.rosestacker.manager.DataManager;
import dev.rosewood.rosestacker.manager.DataManager.StackCounts;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.LocaleManager.TranslationResponse.Result;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import dev.rosewood.rosestacker.utils.StackerUtils;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("rs|rosestacker|stacker")
public class RoseCommand extends BaseCommand {

    protected final RosePlugin rosePlugin;

    public RoseCommand(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        String baseColor = localeManager.getLocaleMessage("base-command-color");
        localeManager.sendCustomMessage(sender, baseColor + "Running <g:#8A2387:#E94057:#F27121>RoseStacker" + baseColor + " v" + this.rosePlugin.getDescription().getVersion());
        localeManager.sendCustomMessage(sender, baseColor + "Plugin created by: <g:#41e0f0:#ff8dce>" + this.rosePlugin.getDescription().getAuthors().get(0));
        localeManager.sendSimpleMessage(sender, "base-command-help");
    }

    @Subcommand("help")
    public void onHelp(CommandSender sender) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        localeManager.sendMessage(sender, "command-help-title");
        localeManager.sendSimpleMessage(sender, "command-convert-description");
        localeManager.sendSimpleMessage(sender, "command-clearall-description");
        localeManager.sendSimpleMessage(sender, "command-give-description");
        localeManager.sendSimpleMessage(sender, "command-help-description");
        localeManager.sendSimpleMessage(sender, "command-purgedata-description");
        localeManager.sendSimpleMessage(sender, "command-querydata-description");
        localeManager.sendSimpleMessage(sender, "command-reload-description");
        localeManager.sendSimpleMessage(sender, "command-stacktool-description");
        localeManager.sendSimpleMessage(sender, "command-stats-description");
        localeManager.sendSimpleMessage(sender, "command-translate-description");
    }

    @Subcommand("reload")
    @CommandPermission("rosestacker.reload")
    public void onReload(CommandSender sender) {
        this.rosePlugin.reload();
        this.rosePlugin.getManager(LocaleManager.class).sendMessage(sender, "command-reload-reloaded");
    }

    @Subcommand("clearall")
    @CommandPermission("rosestacker.clearall")
    @CommandCompletion("@clearallType")
    public void onClearall(CommandSender sender, ClearallType clearallType) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        int amount;
        switch (clearallType) {
            case ENTITY:
                amount = stackManager.removeAllEntityStacks();
                localeManager.sendMessage(sender, "command-clearall-killed-entities", StringPlaceholders.single("amount", amount));
                break;
            case ITEM:
                amount = stackManager.removeAllItemStacks();
                localeManager.sendMessage(sender, "command-clearall-killed-items", StringPlaceholders.single("amount", amount));
                break;
            case ALL:
                int entities = stackManager.removeAllEntityStacks();
                int items = stackManager.removeAllItemStacks();
                localeManager.sendMessage(sender, "command-clearall-killed-all", StringPlaceholders.builder("entityAmount", entities).addPlaceholder("itemAmount", items).build());
                break;
        }
    }

    @Subcommand("convert")
    @CommandPermission("rosestacker.convert")
    @CommandCompletion("@conversionType")
    public void onConvert(CommandSender sender, StackPlugin stackPlugin) {
        ConversionManager conversionManager = this.rosePlugin.getManager(ConversionManager.class);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        if (conversionManager.convert(stackPlugin)) {
            localeManager.sendMessage(sender, "command-convert-converted", StringPlaceholders.single("plugin", stackPlugin.name()));
        } else {
            localeManager.sendMessage(sender, "command-convert-failed", StringPlaceholders.single("plugin", stackPlugin.name()));
        }
    }

    @Subcommand("stats")
    @CommandPermission("rosestacker.stats")
    public void onStats(CommandSender sender) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        int threadAmount = stackManager.getStackingThreads().size();

        int entityStackAmount = stackManager.getStackedEntities().size();
        int itemStackAmount = stackManager.getStackedItems().size();
        int blockStackAmount = stackManager.getStackedBlocks().size();
        int spawnerStackAmount = stackManager.getStackedSpawners().size();

        int entityAmount = stackManager.getStackedEntities().values().stream().mapToInt(Stack::getStackSize).sum();
        int itemAmount = stackManager.getStackedItems().values().stream().mapToInt(Stack::getStackSize).sum();
        int blockAmount = stackManager.getStackedBlocks().values().stream().mapToInt(Stack::getStackSize).sum();
        int spawnerAmount = stackManager.getStackedSpawners().values().stream().mapToInt(Stack::getStackSize).sum();

        localeManager.sendMessage(sender, "command-stats-header");
        localeManager.sendSimpleMessage(sender, "command-stats-threads", StringPlaceholders.single("amount", threadAmount));
        localeManager.sendSimpleMessage(sender, "command-stats-stacked-entities", StringPlaceholders.builder("stackAmount", entityStackAmount).addPlaceholder("total", entityAmount).build());
        localeManager.sendSimpleMessage(sender, "command-stats-stacked-items", StringPlaceholders.builder("stackAmount", itemStackAmount).addPlaceholder("total", itemAmount).build());
        localeManager.sendSimpleMessage(sender, "command-stats-stacked-blocks", StringPlaceholders.builder("stackAmount", blockStackAmount).addPlaceholder("total", blockAmount).build());
        localeManager.sendSimpleMessage(sender, "command-stats-stacked-spawners", StringPlaceholders.builder("stackAmount", spawnerStackAmount).addPlaceholder("total", spawnerAmount).build());
    }

    @Subcommand("give")
    @CommandPermission("rosestacker.give")
    public class GiveCommand extends BaseCommand {

        @Default
        @CatchUnknown
        public void onCommand(CommandSender sender) {
            RoseCommand.this.rosePlugin.getManager(LocaleManager.class).sendMessage(sender, "command-give-usage");
        }

        @Subcommand("block")
        @CommandCompletion("* @stackableBlockMaterial @blockStackAmounts @giveAmounts")
        public void onBlock(CommandSender sender, OnlinePlayer target, Material material, @Conditions("limits:min=1") int stackSize, @Conditions("limits:min=1") @Default("1") int amount) {
            LocaleManager localeManager = RoseCommand.this.rosePlugin.getManager(LocaleManager.class);
            BlockStackSettings stackSettings = RoseCommand.this.rosePlugin.getManager(StackSettingManager.class).getBlockStackSettings(material);
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendMessage(sender, "command-give-unstackable");
                return;
            }

            if (stackSize > stackSettings.getMaxStackSize()) {
                localeManager.sendMessage(sender, "command-give-too-large");
                return;
            }

            Player player = target.getPlayer();
            ItemStack item = StackerUtils.getBlockAsStackedItemStack(material, stackSize);
            this.giveDuplicates(player, item, amount);

            String displayString = localeManager.getLocaleMessage("block-stack-display", StringPlaceholders.builder("amount", stackSize)
                    .addPlaceholder("name", stackSettings.getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName())
                    .addPlaceholder("amount", amount)
                    .addPlaceholder("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendMessage(sender, "command-give-given", placeholders);
            } else {
                localeManager.sendMessage(sender, "command-give-given-multiple", placeholders);
            }
        }

        @Subcommand("spawner")
        @CommandCompletion("* @spawnableSpawnerEntityType @spawnerStackAmounts @giveAmounts")
        public void onSpawner(CommandSender sender, OnlinePlayer target, EntityType entityType, @Conditions("limits:min=1") int stackSize, @Conditions("limits:min=1") @Default("1") int amount) {
            LocaleManager localeManager = RoseCommand.this.rosePlugin.getManager(LocaleManager.class);
            SpawnerStackSettings stackSettings = RoseCommand.this.rosePlugin.getManager(StackSettingManager.class).getSpawnerStackSettings(entityType);
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendMessage(sender, "command-give-unstackable");
                return;
            }

            if (stackSize > stackSettings.getMaxStackSize()) {
                localeManager.sendMessage(sender, "command-give-too-large");
                return;
            }

            Player player = target.getPlayer();
            ItemStack item = StackerUtils.getSpawnerAsStackedItemStack(entityType, stackSize);
            this.giveDuplicates(player, item, amount);

            String displayString;
            if (amount == 1) {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display-item-single", StringPlaceholders.single("name", stackSettings.getDisplayName()));
            } else {
                displayString = RoseStacker.getInstance().getManager(LocaleManager.class).getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", amount)
                        .addPlaceholder("name", stackSettings.getDisplayName()).build());
            }

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName())
                    .addPlaceholder("amount", amount)
                    .addPlaceholder("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendMessage(sender, "command-give-given", placeholders);
            } else {
                localeManager.sendMessage(sender, "command-give-given-multiple", placeholders);
            }
        }

        @Subcommand("entity")
        @CommandCompletion("* @spawnableEggEntityType @entityStackAmounts @giveAmounts")
        public void onEntity(CommandSender sender, OnlinePlayer target, EntityType entityType, @Conditions("limits:min=1") int stackSize, @Conditions("limits:min=1") @Default("1") int amount) {
            LocaleManager localeManager = RoseCommand.this.rosePlugin.getManager(LocaleManager.class);
            EntityStackSettings stackSettings = RoseCommand.this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType);
            if (stackSettings == null || !stackSettings.isStackingEnabled()) {
                localeManager.sendMessage(sender, "command-give-unstackable");
                return;
            }

            if (stackSize > stackSettings.getMaxStackSize()) {
                localeManager.sendMessage(sender, "command-give-too-large");
                return;
            }

            Player player = target.getPlayer();
            ItemStack item = StackerUtils.getEntityAsStackedItemStack(entityType, stackSize);
            if (item == null) {
                RoseCommand.this.rosePlugin.getManager(LocaleManager.class).sendMessage(sender, "command-give-usage");
                return;
            }

            this.giveDuplicates(player, item, amount);

            String displayString = localeManager.getLocaleMessage("entity-stack-display", StringPlaceholders.builder("amount", stackSize)
                    .addPlaceholder("name", stackSettings.getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName())
                    .addPlaceholder("amount", amount)
                    .addPlaceholder("display", displayString)
                    .build();

            if (amount == 1) {
                localeManager.sendMessage(sender, "command-give-given", placeholders);
            } else {
                localeManager.sendMessage(sender, "command-give-given-multiple", placeholders);
            }
        }

        /**
         * Gives a Player duplicates of a single item.
         * Prioritizes giving to the Player inventory and will drop extras on the ground.
         *
         * @param player The Player to give duplicates to
         * @param item The ItemStack to give
         * @param amount The amount of the ItemStack to give
         */
        private void giveDuplicates(Player player, ItemStack item, int amount) {
            ItemStack[] items = new ItemStack[amount];
            Arrays.fill(items, item);
            StackerUtils.dropItemsToPlayer(player, Arrays.asList(items));
        }

    }

    @Subcommand("purgedata")
    @CommandPermission("rosestacker.purgedata")
    @CommandCompletion("@worlds")
    public void onPurgeData(CommandSender sender, String world) {
        DataManager dataManager = this.rosePlugin.getManager(DataManager.class);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
        int totalDeleted = dataManager.purgeData(world);
        if (totalDeleted == 0) {
            localeManager.sendMessage(sender, "command-purgedata-none");
        } else {
            localeManager.sendMessage(sender, "command-purgedata-purged", StringPlaceholders.single("amount", totalDeleted));
        }
    }

    @Subcommand("querydata")
    @CommandPermission("rosestacker.querydata")
    @CommandCompletion("@worlds")
    public void onQueryData(CommandSender sender, World world) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
        DataManager dataManager = this.rosePlugin.getManager(DataManager.class);
        StackCounts stackCounts = dataManager.queryData(world.getName());
        int entity = stackCounts.getEntityCount();
        int item = stackCounts.getItemCount();
        int block = stackCounts.getBlockCount();
        int spawner = stackCounts.getSpawnerCount();
        if (entity == 0 && item == 0 && block == 0 && spawner == 0) {
            localeManager.sendMessage(sender, "command-querydata-none");
        } else {
            localeManager.sendMessage(sender, "command-querydata-header");
            localeManager.sendSimpleMessage(sender, "command-querydata-entity", StringPlaceholders.single("amount", entity));
            localeManager.sendSimpleMessage(sender, "command-querydata-item", StringPlaceholders.single("amount", item));
            localeManager.sendSimpleMessage(sender, "command-querydata-block", StringPlaceholders.single("amount", block));
            localeManager.sendSimpleMessage(sender, "command-querydata-spawner", StringPlaceholders.single("amount", spawner));
        }
    }

    @Subcommand("translate")
    @CommandPermission("rosestacker.translate")
    @CommandCompletion("@translationLocales *")
    public void onTranslate(CommandSender sender, String locale, @Optional String spawnerFormat) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
        StackSettingManager stackSettingManager = this.rosePlugin.getManager(StackSettingManager.class);

        if (spawnerFormat == null) {
            spawnerFormat = "{}";
            localeManager.sendMessage(sender, "command-translate-spawner-format");
        }

        if (!spawnerFormat.contains("{}")) {
            localeManager.sendMessage(sender, "command-translate-spawner-format-invalid");
            return;
        }

        localeManager.sendMessage(sender, "command-translate-loading");

        String finalSpawnerFormat = spawnerFormat;
        localeManager.getMinecraftTranslationValues(locale, response -> {
            if (response.getResult() == Result.FAILURE) {
                localeManager.sendMessage(sender, "command-translate-failure");
                return;
            }

            if (response.getResult() == Result.INVALID_LOCALE) {
                localeManager.sendMessage(sender, "command-translate-invalid-locale");
                return;
            }

            CommentedFileConfiguration blockStackConfig = CommentedFileConfiguration.loadConfiguration(stackSettingManager.getBlockSettingsFile());
            CommentedFileConfiguration entityStackConfig = CommentedFileConfiguration.loadConfiguration(stackSettingManager.getEntitySettingsFile());
            CommentedFileConfiguration itemStackConfig = CommentedFileConfiguration.loadConfiguration(stackSettingManager.getItemSettingsFile());
            CommentedFileConfiguration spawnerStackConfig = CommentedFileConfiguration.loadConfiguration(stackSettingManager.getSpawnerSettingsFile());

            Map<Material, String> materialValues = response.getMaterialValues();
            Map<EntityType, String> entityValues = response.getEntityValues();

            for (Entry<Material, String> entry : materialValues.entrySet()) {
                Material material = entry.getKey();
                String value = entry.getValue();

                if (blockStackConfig.isConfigurationSection(material.name()))
                    blockStackConfig.set(material.name() + ".display-name", value);

                if (itemStackConfig.isConfigurationSection(material.name()))
                    itemStackConfig.set(material.name() + ".display-name", value);
            }

            for (Entry<EntityType, String> entry : entityValues.entrySet()) {
                EntityType entityType = entry.getKey();
                String value = entry.getValue();

                if (entityStackConfig.isConfigurationSection(entityType.name()))
                    entityStackConfig.set(entityType.name() + ".display-name", value);

                if (spawnerStackConfig.isConfigurationSection(entityType.name())) {
                    String name = finalSpawnerFormat.replaceAll(Pattern.quote("{}"), value);
                    spawnerStackConfig.set(entityType.name() + ".display-name", name);
                }
            }

            blockStackConfig.save();
            entityStackConfig.save();
            itemStackConfig.save();
            spawnerStackConfig.save();

            this.rosePlugin.reload();
            localeManager.sendMessage(sender, "command-translate-success");
        });
    }

    @Subcommand("stacktool")
    @CommandPermission("rosestacker.stacktool.give")
    @CommandCompletion("*")
    public void onStackToolOther(CommandSender sender, OnlinePlayer target) {
        Player player = target.getPlayer();
        player.getInventory().addItem(StackerUtils.getStackingTool());
        LocaleManager localeManager = RoseStacker.getInstance().getManager(LocaleManager.class);
        if (sender == player) {
            localeManager.sendMessage(player, "command-stacktool-given");
        } else {
            localeManager.sendMessage(sender, "command-stacktool-given-other", StringPlaceholders.single("player", player.getName()));
        }
    }

    @Subcommand("stacktool")
    @CommandPermission("rosestacker.stacktool.give")
    public void onStackTool(Player player) {
        player.getInventory().addItem(StackerUtils.getStackingTool());
        RoseStacker.getInstance().getManager(LocaleManager.class).sendMessage(player, "command-stacktool-given");
    }

    public enum ClearallType {
        ENTITY,
        ITEM,
        ALL
    }

}
