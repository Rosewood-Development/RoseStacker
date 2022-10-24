package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.command.type.StackedEntityAmount;
import dev.rosewood.rosestacker.command.type.StackedSpawnerAmount;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.SpawnerStackSettings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.EntityType;

public class StackedEntityAmountArgumentHandler extends RoseCommandArgumentHandler<StackedEntityAmount> {

    public StackedEntityAmountArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackedEntityAmount.class);
    }

    @Override
    protected StackedEntityAmount handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();
        try {
            return new StackedEntityAmount(Integer.parseInt(input));
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-stackamount", StringPlaceholders.single("input", input));
        }
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String previous = argumentParser.previous();
        argumentParser.next();

        EntityType entityType = previous == null ? null : Arrays.stream(EntityType.values()).filter(x -> x.name().equalsIgnoreCase(previous)).findFirst().orElse(null);
        if (entityType == null)
            return Collections.singletonList("<stackSize>");

        EntityStackSettings spawnerStackSettings = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType);
        if (spawnerStackSettings == null)
            return Collections.singletonList("<stackSize>");

        int maxStackAmount = spawnerStackSettings.getMaxStackSize();
        return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<stackSize>");
    }

}
