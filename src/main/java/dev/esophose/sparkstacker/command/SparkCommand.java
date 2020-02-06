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
import dev.esophose.sparkstacker.manager.LocaleManager.Locale;
import dev.esophose.sparkstacker.manager.StackManager;
import dev.esophose.sparkstacker.utils.StackerUtils;
import dev.esophose.sparkstacker.utils.StringPlaceholders;
import org.bukkit.ChatColor;
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
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Locale.PREFIX.get() + "&7Plugin created by &5" + this.sparkStacker.getDescription().getAuthors().get(0) + "&7. (&ev" + this.sparkStacker.getDescription().getVersion() + "&7)"));
        localeManager.sendMessage(sender, Locale.COMMAND_RELOAD_DESCRIPTION);
        localeManager.sendMessage(sender, Locale.COMMAND_GIVE_DESCRIPTION);
        localeManager.sendMessage(sender, Locale.COMMAND_CLEARALL_DESCRIPTION);
        localeManager.sendMessage(sender, Locale.COMMAND_CONVERT_DESCRIPTION);
        sender.sendMessage("");
    }

    @Subcommand("reload")
    @Description("Reloads the plugin")
    @CommandPermission("sparkstacker.reload")
    public void onReload(CommandSender sender) {
        this.sparkStacker.reload();
        this.sparkStacker.getLocaleManager().sendPrefixedMessage(sender, Locale.COMMAND_RELOAD_RELOADED);
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
                localeManager.sendPrefixedMessage(sender, Locale.COMMAND_CLEARALL_KILLED_ENTITIES, StringPlaceholders.single("amount", amount));
                break;
            case ITEM:
                amount = stackManager.removeAllItems();
                localeManager.sendPrefixedMessage(sender, Locale.COMMAND_CLEARALL_KILLED_ITEMS, StringPlaceholders.single("amount", amount));
                break;
        }
    }

    @Subcommand("clearall")
    public void onClearall(CommandSender sender) {
        this.sparkStacker.getLocaleManager().sendPrefixedMessage(sender, Locale.COMMAND_CLEARALL_USAGE);
    }

    @Subcommand("convert")
    @Description("Converts a stack plugin's data to this one")
    @CommandPermission("sparkstacker.convert")
    @CommandCompletion("@conversionType")
    public void onConvert(CommandSender sender, StackPlugin stackPlugin) {
        if (this.sparkStacker.getConversionManager().convert(stackPlugin)) {
            this.sparkStacker.getLocaleManager().sendPrefixedMessage(sender, Locale.COMMAND_CONVERT_CONVERTED, StringPlaceholders.single("plugin", stackPlugin.name()));
        } else {
            this.sparkStacker.getLocaleManager().sendPrefixedMessage(sender, Locale.COMMAND_CONVERT_FAILED, StringPlaceholders.single("plugin", stackPlugin.name()));
        }
    }

    @Subcommand("convert")
    public void onConvert(CommandSender sender) {
        this.sparkStacker.getLocaleManager().sendPrefixedMessage(sender, Locale.COMMAND_CONVERT_USAGE);
    }

    @Subcommand("give")
    @Description("Gives stacked items")
    @CommandPermission("sparkstacker.give")
    public class GiveCommand extends BaseCommand {

        @Default
        @CatchUnknown
        public void onCommand(CommandSender sender) {
            SparkCommand.this.sparkStacker.getLocaleManager().sendPrefixedMessage(sender, Locale.COMMAND_GIVE_USAGE);
        }

        @Subcommand("block")
        @CommandCompletion("* @stackableBlockMaterial @blockStackAmounts")
        public void onBlock(OnlinePlayer target, Material material, int amount) {
            Player player = target.getPlayer();
            player.getInventory().addItem(StackerUtils.getBlockAsStackedItemStack(material, amount));

            String displayString = ChatColor.translateAlternateColorCodes('&', StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", SparkCommand.this.sparkStacker.getStackSettingManager().getBlockStackSettings(material).getDisplayName())
                    .apply(Locale.BLOCK_STACK_DISPLAY.get()));

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            SparkCommand.this.sparkStacker.getLocaleManager().sendPrefixedMessage(player, Locale.COMMAND_GIVE_GIVEN, placeholders);
        }

        @Subcommand("spawner")
        @CommandCompletion("* @spawnableEntityType @spawnerStackAmounts")
        public void onSpawner(OnlinePlayer target, EntityType entityType, int amount) {
            Player player = target.getPlayer();
            target.getPlayer().getInventory().addItem(StackerUtils.getSpawnerAsStackedItemStack(entityType, amount));

            String displayString = ChatColor.translateAlternateColorCodes('&', StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", SparkCommand.this.sparkStacker.getStackSettingManager().getSpawnerStackSettings(entityType).getDisplayName())
                    .apply(Locale.SPAWNER_STACK_DISPLAY.get()));

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            SparkCommand.this.sparkStacker.getLocaleManager().sendPrefixedMessage(player, Locale.COMMAND_GIVE_GIVEN, placeholders);
        }

        @Subcommand("entity")
        @CommandCompletion("* @spawnableEntityType @entityStackAmounts")
        public void onEntity(OnlinePlayer target, EntityType entityType, int amount) {
            Player player = target.getPlayer();
            target.getPlayer().getInventory().addItem(StackerUtils.getEntityAsStackedItemStack(entityType, amount));

            String displayString = ChatColor.translateAlternateColorCodes('&', StringPlaceholders.builder("amount", amount)
                    .addPlaceholder("name", SparkCommand.this.sparkStacker.getStackSettingManager().getEntityStackSettings(entityType).getDisplayName())
                    .apply(Locale.ENTITY_STACK_DISPLAY.get()));

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            SparkCommand.this.sparkStacker.getLocaleManager().sendPrefixedMessage(player, Locale.COMMAND_GIVE_GIVEN, placeholders);
        }

    }

    public enum ClearallType {
        ENTITY,
        ITEM
    }

}
