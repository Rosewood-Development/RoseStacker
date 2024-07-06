package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.EntityType;

public class StackedSpawnerTypeArgumentHandler extends ArgumentHandler<SpawnerType> {

    private final RosePlugin rosePlugin;

    public StackedSpawnerTypeArgumentHandler(RosePlugin rosePlugin) {
        super(SpawnerType.class);

        this.rosePlugin = rosePlugin;
    }

    @Override
    public SpawnerType handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        if (NMSAdapter.getHandler().supportsEmptySpawners() && input.equalsIgnoreCase("empty"))
            return SpawnerType.empty();

        return Arrays.stream(EntityType.values())
                .filter(x -> x.name().equalsIgnoreCase(input))
                .map(SpawnerType::of)
                .findFirst()
                .orElseThrow(() -> new HandledArgumentException("argument-handler-stacktype", StringPlaceholders.of("input", input)));
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return this.rosePlugin.getManager(StackSettingManager.class).getStackableSpawnerTypes().stream()
                .map(SpawnerType::getEnumName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

}
