package dev.esophose.rosestacker.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.LocaleManager.Locale;
import dev.esophose.rosestacker.utils.StackerUtils;
import dev.esophose.rosestacker.utils.StringPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@CommandAlias("rs|rosestacker|stacker")
@Description("The base RoseStacker command")
public class RoseCommand extends BaseCommand {

    protected final RoseStacker roseStacker;

    public RoseCommand(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Locale.PREFIX.get() + "&7Version " + this.roseStacker.getDescription().getVersion() + " created by &5" + this.roseStacker.getDescription().getAuthors().get(0)));
        this.roseStacker.getLocaleManager().sendMessage(sender, Locale.COMMAND_RELOAD_DESCRIPTION);
        this.roseStacker.getLocaleManager().sendMessage(sender, Locale.COMMAND_GIVE_DESCRIPTION);
        sender.sendMessage("");
    }

    @Subcommand("reload")
    @Description("Reloads the plugin")
    @CommandPermission("rosestacker.reload")
    public void onReload(CommandSender sender) {
        this.roseStacker.reload();
        this.roseStacker.getLocaleManager().sendPrefixedMessage(sender, Locale.COMMAND_RELOAD_RELOADED);
    }

    @Subcommand("give")
    @Description("Gives stacked items")
    @CommandPermission("rosestacker.give")
    public class GiveCommand extends BaseCommand {

        @Default
        @CatchUnknown
        public void onCommand(CommandSender sender) {
            roseStacker.getLocaleManager().sendPrefixedMessage(sender, Locale.COMMAND_GIVE_USAGE);
        }

        @Subcommand("block")
        @CommandCompletion("* @stackableBlockMaterial @blockStackAmounts")
        public void onBlock(OnlinePlayer target, Material material, int amount) {
            Player player = target.getPlayer();
            player.getInventory().addItem(StackerUtils.getBlockAsStackedItemStack(material, amount));

            String displayString = ChatColor.translateAlternateColorCodes('&', StringPlaceholders.builder("amount", String.valueOf(amount))
                    .addPlaceholder("name", roseStacker.getStackSettingManager().getBlockStackSettings(material).getDisplayName())
                    .apply(Locale.BLOCK_STACK_DISPLAY.get()));

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            roseStacker.getLocaleManager().sendPrefixedMessage(player, Locale.COMMAND_GIVE_GIVEN, placeholders);
        }

        @Subcommand("spawner")
        @CommandCompletion("* @spawnableEntityType @spawnerStackAmounts")
        public void onSpawner(OnlinePlayer target, EntityType entityType, int amount) {
            Player player = target.getPlayer();
            target.getPlayer().getInventory().addItem(StackerUtils.getSpawnerAsStackedItemStack(entityType, amount));

            String displayString = ChatColor.translateAlternateColorCodes('&', StringPlaceholders.builder("amount", String.valueOf(amount))
                    .addPlaceholder("name", roseStacker.getStackSettingManager().getSpawnerStackSettings(entityType).getDisplayName())
                    .apply(Locale.SPAWNER_STACK_DISPLAY.get()));

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            roseStacker.getLocaleManager().sendPrefixedMessage(player, Locale.COMMAND_GIVE_GIVEN, placeholders);
        }

        @Subcommand("entity")
        @CommandCompletion("* @spawnableEntityType @entityStackAmounts")
        public void onEntity(OnlinePlayer target, EntityType entityType, int amount) {
            Player player = target.getPlayer();
            target.getPlayer().getInventory().addItem(StackerUtils.getEntityAsStackedItemStack(entityType, amount));

            String displayString = ChatColor.translateAlternateColorCodes('&', StringPlaceholders.builder("amount", String.valueOf(amount))
                    .addPlaceholder("name", roseStacker.getStackSettingManager().getEntityStackSettings(entityType).getDisplayName())
                    .apply(Locale.ENTITY_STACK_DISPLAY.get()));

            StringPlaceholders placeholders = StringPlaceholders.builder("player", player.getName()).addPlaceholder("display", displayString).build();
            roseStacker.getLocaleManager().sendPrefixedMessage(player, Locale.COMMAND_GIVE_GIVEN, placeholders);
        }

    }

}
