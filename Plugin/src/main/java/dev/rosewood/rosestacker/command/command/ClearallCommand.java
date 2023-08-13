package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.utils.StackerUtils;

public class ClearallCommand extends RoseCommand {

    public ClearallCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, ClearallType type) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        int amount;
        switch (type) {
            case ENTITY -> {
                amount = stackManager.removeAllEntityStacks();
                localeManager.sendMessage(context.getSender(), "command-clearall-killed-entities", StringPlaceholders.of("amount", StackerUtils.formatNumber(amount)));
            }
            case ITEM -> {
                amount = stackManager.removeAllItemStacks();
                localeManager.sendMessage(context.getSender(), "command-clearall-killed-items", StringPlaceholders.of("amount", StackerUtils.formatNumber(amount)));
            }
            case ALL -> {
                int entities = stackManager.removeAllEntityStacks();
                int items = stackManager.removeAllItemStacks();
                localeManager.sendMessage(context.getSender(), "command-clearall-killed-all", StringPlaceholders.builder("entityAmount", StackerUtils.formatNumber(entities)).add("itemAmount", StackerUtils.formatNumber(items)).build());
            }
        }
    }

    @Override
    protected String getDefaultName() {
        return "clearall";
    }

    @Override
    public String getDescriptionKey() {
        return "command-clearall-description";
    }

    @Override
    public String getRequiredPermission() {
        return "rosestacker.clearall";
    }

    public enum ClearallType {
        ENTITY,
        ITEM,
        ALL
    }

}
