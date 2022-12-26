package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.command.type.StackedSpawnerType;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.EntityType;

public class StackedSpawnerTypeArgumentHandler extends RoseCommandArgumentHandler<StackedSpawnerType> {

    public StackedSpawnerTypeArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackedSpawnerType.class);
    }

    @Override
    protected StackedSpawnerType handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();
        if (NMSAdapter.getHandler().supportsEmptySpawners() && input.equalsIgnoreCase("empty"))
            return new StackedSpawnerType(SpawnerType.empty());

        return Arrays.stream(EntityType.values())
                .filter(x -> x.name().equalsIgnoreCase(input))
                .map(SpawnerType::of)
                .map(StackedSpawnerType::new)
                .findFirst()
                .orElseThrow(() -> new HandledArgumentException("argument-handler-stacktype", StringPlaceholders.single("input", input)));
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return this.rosePlugin.getManager(StackSettingManager.class).getStackableSpawnerTypes().stream()
                .map(SpawnerType::getEnumName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

}
