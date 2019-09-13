package dev.esophose.rosestacker.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.LocaleManager.Locale;
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
    public void onCommand(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Locale.PREFIX.get() + "&7Version " + this.roseStacker.getDescription().getVersion() + " created by &5" + this.roseStacker.getDescription().getAuthors().get(0)));
        this.roseStacker.getLocaleManager().sendMessage(sender, Locale.COMMAND_RELOAD_DESCRIPTION);
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

        @Subcommand("block")
        @CommandCompletion("@amount @stackableBlockMaterial")
        public void onBlock(Player sender, int amount, Material material) {

        }

        @Subcommand("spawner")
        @CommandCompletion("@amount @spawnableEntityType")
        public void onSpawner(Player sender, int amount, EntityType entityType) {

        }

        @Subcommand("entity")
        @CommandCompletion("@amount @spawnableEntityType")
        public void onEntity(Player sender, int amount, EntityType entityType) {

        }

    }

}
