package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.Optional;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.utils.ItemUtils;
import org.bukkit.entity.Player;

public class StackToolCommand extends RoseCommand {

    public StackToolCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, @Optional Player target) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        if (target == null) {
            if (!(context.getSender() instanceof Player)) {
                localeManager.sendMessage(context.getSender(), "command-stacktool-no-console");
                return;
            }

            Player player = (Player) context.getSender();
            player.getInventory().addItem(ItemUtils.getStackingTool());
            RoseStacker.getInstance().getManager(LocaleManager.class).sendMessage(player, "command-stacktool-given");
        } else {
            target.getInventory().addItem(ItemUtils.getStackingTool());
            localeManager.sendMessage(target, "command-stacktool-given");
            if (context.getSender() != target)
                localeManager.sendMessage(context.getSender(), "command-stacktool-given-other", StringPlaceholders.single("player", target.getName()));
        }
    }

    @Override
    protected String getDefaultName() {
        return "stacktool";
    }

    @Override
    public String getDescriptionKey() {
        return "command-stacktool-description";
    }

    @Override
    public String getRequiredPermission() {
        return "rosestacker.stacktool";
    }

}
