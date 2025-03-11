package dev.rosewood.rosestacker.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.utils.StackerUtils;

public class ClearallCommand extends BaseRoseCommand {

    public ClearallCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @RoseExecutable
    public void execute(CommandContext context, ClearallType type) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        int amount;
        switch (type) {
            case ENTITY -> {
                amount = stackManager.removeAllEntityStacks();
                localeManager.sendCommandMessage(context.getSender(), "command-clearall-killed-entities", StringPlaceholders.of("amount", StackerUtils.formatNumber(amount)));
            }
            case ITEM -> {
                amount = stackManager.removeAllItemStacks();
                localeManager.sendCommandMessage(context.getSender(), "command-clearall-killed-items", StringPlaceholders.of("amount", StackerUtils.formatNumber(amount)));
            }
            case ALL -> {
                int entities = stackManager.removeAllEntityStacks();
                int items = stackManager.removeAllItemStacks();
                localeManager.sendCommandMessage(context.getSender(), "command-clearall-killed-all", StringPlaceholders.builder("entityAmount", StackerUtils.formatNumber(entities)).add("itemAmount", StackerUtils.formatNumber(items)).build());
            }
        }
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("clearall")
                .descriptionKey("command-clearall-description")
                .permission("rosestacker.clearall")
                .arguments(ArgumentsDefinition.builder()
                        .required("type", ArgumentHandlers.forEnum(ClearallType.class))
                        .build())
                .build();
    }

    public enum ClearallType {
        ENTITY,
        ITEM,
        ALL
    }

}
