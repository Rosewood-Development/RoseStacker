package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.command.type.StackedEntityType;
import dev.rosewood.rosestacker.command.type.StackedSpawnerType;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.EntityType;

public class StackedEntityTypeArgumentHandler extends RoseCommandArgumentHandler<StackedEntityType> {

    public StackedEntityTypeArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackedEntityType.class);
    }

    @Override
    protected StackedEntityType handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();
        return Arrays.stream(EntityType.values())
                .filter(x -> x.name().equalsIgnoreCase(input))
                .map(StackedEntityType::new)
                .findFirst()
                .orElseThrow(() -> new HandledArgumentException("argument-handler-stacktype", StringPlaceholders.single("input", input)));
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return this.rosePlugin.getManager(StackSettingManager.class).getStackableEntityTypes().stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

}
