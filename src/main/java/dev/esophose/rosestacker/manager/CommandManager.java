package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandManager extends Manager implements CommandExecutor, TabCompleter {

    public CommandManager(RoseStacker roseStacker) {
        super(roseStacker);

        PluginCommand command = roseStacker.getCommand("rosestacker");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        LocaleManager localeManager = this.roseStacker.getLocaleManager();

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (this.doesntHavePermission(commandSender, "rosestacker.reload", localeManager))
                    return true;

                this.roseStacker.reload();
                localeManager.sendPrefixedMessage(commandSender, LocaleManager.Locale.COMMAND_RELOAD_RELOADED);
                return true;
            }
        }

        commandSender.sendMessage("");
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.Locale.PREFIX.get() + "&7Version " + this.roseStacker.getDescription().getVersion() + " created by &5" + this.roseStacker.getDescription().getAuthors().get(0)));
        localeManager.sendMessage(commandSender, LocaleManager.Locale.COMMAND_RELOAD_DESCRIPTION);
        commandSender.sendMessage("");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length < 1)
            return completions;

        Set<String> possibleCompletions = new HashSet<>();

        if (commandSender.hasPermission("rosestacker.reload"))
            possibleCompletions.add("reload");

        StringUtil.copyPartialMatches(args[0], possibleCompletions, completions);

        return completions;
    }

    /**
     * Checks if a player does have a permission
     * Sends them an error message if they don't
     *
     * @param sender The CommandSender to check
     * @param permission The permission to check for
     * @param localeManager The LocaleManager instance
     * @return True if the player has permission, otherwise false and sends a message
     */
    private boolean doesntHavePermission(CommandSender sender, String permission, LocaleManager localeManager) {
        if (!sender.hasPermission(permission)) {
            localeManager.sendPrefixedMessage(sender, LocaleManager.Locale.NO_PERMISSION);
            return true;
        }
        return false;
    }

}
