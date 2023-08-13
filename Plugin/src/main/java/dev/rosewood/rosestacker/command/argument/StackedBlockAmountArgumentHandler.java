package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.command.argument.StackedBlockAmountArgumentHandler.StackedBlockAmount;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.BlockStackSettings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;

public class StackedBlockAmountArgumentHandler extends RoseCommandArgumentHandler<StackedBlockAmount> {

    public StackedBlockAmountArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackedBlockAmount.class);
    }

    @Override
    protected StackedBlockAmount handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();
        try {
            return new StackedBlockAmount(Integer.parseInt(input));
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-stackamount", StringPlaceholders.of("input", input));
        }
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String previous = argumentParser.previous();
        argumentParser.next();

        Material blockType = previous == null ? null : Material.matchMaterial(previous);
        if (blockType == null)
            return Collections.singletonList("<stackSize>");

        BlockStackSettings blockStackSettings = this.rosePlugin.getManager(StackSettingManager.class).getBlockStackSettings(blockType);
        if (blockStackSettings == null)
            return Collections.singletonList("<stackSize>");

        int maxStackAmount = blockStackSettings.getMaxStackSize();
        return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<stackSize>");
    }

    public record StackedBlockAmount(int amount) { }

}
