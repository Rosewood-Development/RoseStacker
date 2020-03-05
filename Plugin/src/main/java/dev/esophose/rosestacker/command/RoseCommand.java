package dev.esophose.rosestacker.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.StackPlugin;
import dev.esophose.rosestacker.manager.ConversionManager;
import dev.esophose.rosestacker.manager.DataManager;
import dev.esophose.rosestacker.manager.DataManager.StackCounts;
import dev.esophose.rosestacker.manager.LocaleManager;
import dev.esophose.rosestacker.manager.StackManager;
import dev.esophose.rosestacker.manager.StackSettingManager;
import dev.esophose.rosestacker.utils.StackerUtils;
import dev.esophose.rosestacker.utils.StringPlaceholders;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("rs|rosestacker|stacker")
public class RoseCommand extends BaseCommand {

    protected final RoseStacker roseStacker;

    public RoseCommand(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);

        sender.sendMessage("");
        localeManager.sendCustomMessage(sender, localeManager.getLocaleMessage("prefix") + "&7Plugin created by &5" + this.roseStacker.getDescription().getAuthors().get(0) + "&7. (&ev" + this.roseStacker.getDescription().getVersion() + "&7)");
        localeManager.sendSimpleMessage(sender, "command-reload-description");
        localeManager.sendSimpleMessage(sender, "command-give-description");
        localeManager.sendSimpleMessage(sender, "command-clearall-description");
        localeManager.sendSimpleMessage(sender, "command-stats-description");
        localeManager.sendSimpleMessage(sender, "command-convert-description");
        localeManager.sendSimpleMessage(sender, "command-purgedata-description");
        localeManager.sendSimpleMessage(sender, "command-querydata-description");
        sender.sendMessage("");
    }

    @Subcommand("reload")
    @CommandPermission("rosestacker.reload")
    public void onReload(CommandSender sender) {
        this.roseStacker.reload();
        this.roseStacker.getManager(LocaleManager.class).sendMessage(sender, "command-reload-reloaded");
    }

    @Subcommand("clearall")
    @CommandPermission("rosestacker.clearall")
    @CommandCompletion("@clearallType")
    public void onClearall(CommandSender sender, ClearallType clearallType) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);

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
        }
    }

    @Subcommand("convert")
    @CommandPermission("rosestacker.convert")
    @CommandCompletion("@conversionType")
    public void onConvert(CommandSender sender, StackPlugin stackPlugin) {
        ConversionManager conversionManager = this.roseStacker.getManager(ConversionManager.class);
        LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);

        if (conversionManager.convert(stackPlugin)) {
            localeManager.sendMessage(sender, "command-convert-converted", StringPlaceholders.single("plugin", stackPlugin.name()));
        } else {
            localeManager.sendMessage(sender, "command-convert-failed", StringPlaceholders.single("plugin", stackPlugin.name()));
        }
    }

    @Subcommand("stats")
    @CommandPermission("rosestacker.stats")
    public void onStats(CommandSender sender) {
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);
        LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);

        int threadAmount = stackManager.getStackingThreads().size();
        int entityAmount = stackManager.getStackedEntities().size();
        int itemAmount = stackManager.getStackedItems().size();
        int blockAmount = stackManager.getStackedItems().size();
        int spawnerAmount = stackManager.getStackedSpawners().size();

        localeManager.sendMessage(sender, "command-stats-header");
        localeManager.sendSimpleMessage(sender, "command-stats-threads", StringPlaceholders.single("amount", threadAmount));
        localeManager.sendSimpleMessage(sender, "command-stats-stacked-entities", StringPlaceholders.single("amount", entityAmount));
        localeManager.sendSimpleMessage(sender, "command-stats-stacked-items", StringPlaceholders.single("amount", itemAmount));
        localeManager.sendSimpleMessage(sender, "command-stats-stacked-blocks", StringPlaceholders.single("amount", blockAmount));
        localeManager.sendSimpleMessage(sender, "command-stats-stacked-spawners", StringPlaceholders.single("amount", spawnerAmount));
    }

    @Subcommand("give")
    @CommandPermission("rosestacker.give")
    public class GiveCommand extends BaseCommand {

        @Default
        @CatchUnknown
        public void onCommand(CommandSender sender) {
            RoseCommand.this.roseStacker.getManager(LocaleManager.class).sendMessage(sender, "command-give-usage");
        }

        @Subcommand("block")
        @CommandCompletion("* @stackableBlockMaterial @blockStackAmounts")
        public void onBlock(CommandSender sender, OnlinePlayer target, Material material, @Conditions("limits:min=1") int amount) {
            LocaleManager localeManager = RoseCommand.this.roseStacker.getManager(LocaleManager.class);

            Player player = target.getPlayer();
            player.getInventory().addItem(StackerUtils.getBlockAsStackedItemStack(material, amount));

            String displayString = localeManager.getLocaleMessage("block-stack-display", StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", RoseCommand.this.roseStacker.getManager(StackSettingManager.class).getBlockStackSettings(material).getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            localeManager.sendMessage(sender, "command-give-given", placeholders);
        }

        @Subcommand("spawner")
        @CommandCompletion("* @spawnableSpawnerEntityType @spawnerStackAmounts")
        public void onSpawner(CommandSender sender, OnlinePlayer target, EntityType entityType, @Conditions("limits:min=1") int amount) {
            LocaleManager localeManager = RoseCommand.this.roseStacker.getManager(LocaleManager.class);

            Player player = target.getPlayer();
            target.getPlayer().getInventory().addItem(StackerUtils.getSpawnerAsStackedItemStack(entityType, amount));

            String displayString = localeManager.getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", RoseCommand.this.roseStacker.getManager(StackSettingManager.class).getSpawnerStackSettings(entityType).getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            localeManager.sendMessage(sender, "command-give-given", placeholders);
        }

        @Subcommand("entity")
        @CommandCompletion("* @spawnableEggEntityType @entityStackAmounts")
        public void onEntity(CommandSender sender, OnlinePlayer target, EntityType entityType, @Conditions("limits:min=1") int amount) {
            LocaleManager localeManager = RoseCommand.this.roseStacker.getManager(LocaleManager.class);

            Player player = target.getPlayer();
            ItemStack itemStack = StackerUtils.getEntityAsStackedItemStack(entityType, amount);
            if (itemStack == null) {
                RoseCommand.this.roseStacker.getManager(LocaleManager.class).sendMessage(sender, "command-give-usage");
                return;
            }

            target.getPlayer().getInventory().addItem(itemStack);

            String displayString = localeManager.getLocaleMessage("entity-stack-display", StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", RoseCommand.this.roseStacker.getManager(StackSettingManager.class).getEntityStackSettings(entityType).getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            localeManager.sendMessage(sender, "command-give-given", placeholders);
        }

    }

    @Subcommand("purgedata")
    @CommandPermission("rosestacker.purgedata")
    @CommandCompletion("@worlds")
    public void onPurgeData(CommandSender sender, String world) {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);
        LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);
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
        LocaleManager localeManager = this.roseStacker.getManager(LocaleManager.class);
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);
        StackCounts stackCounts = dataManager.queryData(world.getName());
        int entity = stackCounts.getEntityCount();
        int item = stackCounts.getItemCount();
        int block = stackCounts.getBlockCount();
        int spawner = stackCounts.getSpawnerCount();
        if (entity == 0 && item == 0 && block == 0 && spawner == 0) {
            localeManager.sendMessage(sender, "command-querydata-none");
        } else {
            localeManager.sendMessage(sender, "command-querydata-header");
            localeManager.sendMessage(sender, "command-querydata-entity", StringPlaceholders.single("amount", entity));
            localeManager.sendMessage(sender, "command-querydata-item", StringPlaceholders.single("amount", item));
            localeManager.sendMessage(sender, "command-querydata-block", StringPlaceholders.single("amount", block));
            localeManager.sendMessage(sender, "command-querydata-spawner", StringPlaceholders.single("amount", spawner));
        }
    }

    public enum ClearallType {
        ENTITY,
        ITEM
    }

}
