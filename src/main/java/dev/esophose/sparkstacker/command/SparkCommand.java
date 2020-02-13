package dev.esophose.sparkstacker.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.manager.ConversionManager.StackPlugin;
import dev.esophose.sparkstacker.manager.LocaleManager;
import dev.esophose.sparkstacker.manager.StackManager;
import dev.esophose.sparkstacker.utils.StackerUtils;
import dev.esophose.sparkstacker.utils.StringPlaceholders;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@CommandAlias("ss|sparkstacker|stacker")
@Description("The base SparkStacker command")
public class SparkCommand extends BaseCommand {

    protected final SparkStacker sparkStacker;

    public SparkCommand(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
    }

    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        LocaleManager localeManager = this.sparkStacker.getLocaleManager();

        sender.sendMessage("");
        localeManager.sendMessage(sender, "&7Plugin created by &5" + this.sparkStacker.getDescription().getAuthors().get(0) + "&7. (&ev" + this.sparkStacker.getDescription().getVersion() + "&7)");
        localeManager.sendCustomMessage(sender, "command-reload-description");
        localeManager.sendCustomMessage(sender, "command-give-description");
        localeManager.sendCustomMessage(sender, "command-clearall-description");
        localeManager.sendCustomMessage(sender, "command-convert-description");
        sender.sendMessage("");
    }

    @Subcommand("reload")
    @Description("Reloads the plugin")
    @CommandPermission("sparkstacker.reload")
    public void onReload(CommandSender sender) {
        this.sparkStacker.reload();
        this.sparkStacker.getLocaleManager().sendMessage(sender, "command-reload-reloaded");
    }

    @Subcommand("clearall")
    @Description("Clears all stacked entities or items")
    @CommandPermission("sparkstacker.clearall")
    @CommandCompletion("@clearallType")
    public void onClearall(CommandSender sender, ClearallType clearallType) {
        StackManager stackManager = this.sparkStacker.getStackManager();
        LocaleManager localeManager = this.sparkStacker.getLocaleManager();

        int amount;
        switch (clearallType) {
            case ENTITY:
                amount = stackManager.removeAllEntities();
                localeManager.sendMessage(sender, "command-clearall-killed-entities", StringPlaceholders.single("amount", amount));
                break;
            case ITEM:
                amount = stackManager.removeAllItems();
                localeManager.sendMessage(sender, "command-clearall-killed-items", StringPlaceholders.single("amount", amount));
                break;
        }
    }

    @Subcommand("clearall")
    public void onClearall(CommandSender sender) {
        this.sparkStacker.getLocaleManager().sendMessage(sender, "command-clearall-usage");
    }

    @Subcommand("convert")
    @Description("Converts a stack plugin's data to this one")
    @CommandPermission("sparkstacker.convert")
    @CommandCompletion("@conversionType")
    public void onConvert(CommandSender sender, StackPlugin stackPlugin) {
        if (this.sparkStacker.getConversionManager().convert(stackPlugin)) {
            this.sparkStacker.getLocaleManager().sendMessage(sender, "command-convert-converted", StringPlaceholders.single("plugin", stackPlugin.name()));
        } else {
            this.sparkStacker.getLocaleManager().sendMessage(sender, "command-convert-failed", StringPlaceholders.single("plugin", stackPlugin.name()));
        }
    }

    @Subcommand("convert")
    public void onConvert(CommandSender sender) {
        this.sparkStacker.getLocaleManager().sendMessage(sender, "command-convert-usage");
    }

    @Subcommand("give")
    @Description("Gives stacked items")
    @CommandPermission("sparkstacker.give")
    public class GiveCommand extends BaseCommand {

        @Default
        @CatchUnknown
        public void onCommand(CommandSender sender) {
            SparkCommand.this.sparkStacker.getLocaleManager().sendMessage(sender, "command-give-usage");
        }

        @Subcommand("block")
        @CommandCompletion("* @stackableBlockMaterial @blockStackAmounts")
        public void onBlock(OnlinePlayer target, Material material, int amount) {
            LocaleManager localeManager = SparkCommand.this.sparkStacker.getLocaleManager();

            Player player = target.getPlayer();
            player.getInventory().addItem(StackerUtils.getBlockAsStackedItemStack(material, amount));

            String displayString = localeManager.getLocaleMessage("block-stack-display", StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", SparkCommand.this.sparkStacker.getStackSettingManager().getBlockStackSettings(material).getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            localeManager.sendMessage(player, "command-give-given", placeholders);
        }

        @Subcommand("spawner")
        @CommandCompletion("* @spawnableEntityType @spawnerStackAmounts")
        public void onSpawner(OnlinePlayer target, EntityType entityType, int amount) {
            LocaleManager localeManager = SparkCommand.this.sparkStacker.getLocaleManager();

            Player player = target.getPlayer();
            target.getPlayer().getInventory().addItem(StackerUtils.getSpawnerAsStackedItemStack(entityType, amount));

            String displayString = localeManager.getLocaleMessage("spawner-stack-display", StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", SparkCommand.this.sparkStacker.getStackSettingManager().getSpawnerStackSettings(entityType).getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            localeManager.sendMessage(player, "command-give-given", placeholders);
        }

        @Subcommand("entity")
        @CommandCompletion("* @spawnableEntityType @entityStackAmounts")
        public void onEntity(OnlinePlayer target, EntityType entityType, int amount) {
            LocaleManager localeManager = SparkCommand.this.sparkStacker.getLocaleManager();

            Player player = target.getPlayer();
            target.getPlayer().getInventory().addItem(StackerUtils.getEntityAsStackedItemStack(entityType, amount));

            String displayString = localeManager.getLocaleMessage("entity-stack-display", StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", SparkCommand.this.sparkStacker.getStackSettingManager().getSpawnerStackSettings(entityType).getDisplayName()).build());

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            localeManager.sendMessage(player, "command-give-given", placeholders);
        }

    }

    public enum ClearallType {
        ENTITY,
        ITEM
    }

}
