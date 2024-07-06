package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.utils.ItemUtils;
import org.bukkit.entity.Player;

public class StackToolCommand extends BaseRoseCommand {

    private final RosePlugin rosePlugin;

    public StackToolCommand(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.rosePlugin = rosePlugin;
    }

    @RoseExecutable
    public void execute(CommandContext context, Player target) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        if (target == null) {
            if (!(context.getSender() instanceof Player player)) {
                localeManager.sendCommandMessage(context.getSender(), "command-stacktool-no-console");
                return;
            }

            player.getInventory().addItem(ItemUtils.getStackingTool());
            localeManager.sendCommandMessage(player, "command-stacktool-given");
        } else {
            target.getInventory().addItem(ItemUtils.getStackingTool());
            localeManager.sendCommandMessage(context.getSender(), "command-stacktool-given-other", StringPlaceholders.of("player", target.getName()));
        }
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("stacktool")
                .descriptionKey("command-stacktool-description")
                .permission("rosestacker.stacktool")
                .arguments(ArgumentsDefinition.builder()
                        .optional("target", ArgumentHandlers.PLAYER)
                        .build())
                .build();
    }

}
