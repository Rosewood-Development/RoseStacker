package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.EntityType;

public class StackedEntityTypeArgumentHandler extends ArgumentHandler<EntityType> {

    private final RosePlugin rosePlugin;

    public StackedEntityTypeArgumentHandler(RosePlugin rosePlugin) {
        super(EntityType.class);

        this.rosePlugin = rosePlugin;
    }

    @Override
    public EntityType handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        return Arrays.stream(EntityType.values())
                .filter(x -> x.name().equalsIgnoreCase(input))
                .findFirst()
                .orElseThrow(() -> new HandledArgumentException("argument-handler-stacktype", StringPlaceholders.of("input", input)));
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return this.rosePlugin.getManager(StackSettingManager.class).getStackableEntityTypesWithSpawnEggs().stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

}
