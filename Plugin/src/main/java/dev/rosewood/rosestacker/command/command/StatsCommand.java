package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.LocaleManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.utils.StackerUtils;
import dev.rosewood.rosestacker.utils.ThreadUtils;

public class StatsCommand extends BaseRoseCommand {

    private final RosePlugin rosePlugin;

    public StatsCommand(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.rosePlugin = rosePlugin;
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        int threadAmount = stackManager.getStackingThreads().size();

        int entityStackAmount = stackManager.getStackedEntities().size();
        int itemStackAmount = stackManager.getStackedItems().size();
        int blockStackAmount = stackManager.getStackedBlocks().size();
        int spawnerStackAmount = stackManager.getStackedSpawners().size();

        int entityAmount = stackManager.getStackedEntities().values().stream().mapToInt(Stack::getStackSize).sum();
        int itemAmount = stackManager.getStackedItems().values().stream().mapToInt(Stack::getStackSize).sum();
        int blockAmount = stackManager.getStackedBlocks().values().stream().mapToInt(Stack::getStackSize).sum();
        int spawnerAmount = stackManager.getStackedSpawners().values().stream().mapToInt(Stack::getStackSize).sum();

        localeManager.sendCommandMessage(context.getSender(), "command-stats-header");
        localeManager.sendSimpleCommandMessage(context.getSender(), "command-stats-threads", StringPlaceholders.of("amount", StackerUtils.formatNumber(threadAmount)));
        localeManager.sendSimpleCommandMessage(context.getSender(), "command-stats-stacked-entities", StringPlaceholders.builder("stackAmount", entityStackAmount).add("total", StackerUtils.formatNumber(entityAmount)).build());
        localeManager.sendSimpleCommandMessage(context.getSender(), "command-stats-stacked-items", StringPlaceholders.builder("stackAmount", itemStackAmount).add("total", StackerUtils.formatNumber(itemAmount)).build());
        localeManager.sendSimpleCommandMessage(context.getSender(), "command-stats-stacked-blocks", StringPlaceholders.builder("stackAmount", blockStackAmount).add("total", StackerUtils.formatNumber(blockAmount)).build());
        localeManager.sendSimpleCommandMessage(context.getSender(), "command-stats-stacked-spawners", StringPlaceholders.builder("stackAmount", spawnerStackAmount).add("total", StackerUtils.formatNumber(spawnerAmount)).build());
        localeManager.sendSimpleCommandMessage(context.getSender(), "command-stats-active-tasks", StringPlaceholders.of("amount", StackerUtils.formatNumber(ThreadUtils.getActiveThreads())));
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("stats")
                .descriptionKey("command-stats-description")
                .permission("rosestacker.stats")
                .build();
    }

}
