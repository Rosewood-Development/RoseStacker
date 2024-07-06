package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.EntityType;

public class StackedEntityAmountArgumentHandler extends ArgumentHandler<Integer> {

    private final RosePlugin rosePlugin;

    public StackedEntityAmountArgumentHandler(RosePlugin rosePlugin) {
        super(Integer.class);

        this.rosePlugin = rosePlugin;
    }

    @Override
    public Integer handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-stackamount", StringPlaceholders.of("input", input));
        }
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        EntityType entityType = context.get(EntityType.class);
        if (entityType == null)
            return Collections.singletonList("<stackSize>");

        EntityStackSettings spawnerStackSettings = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(entityType);
        if (spawnerStackSettings == null)
            return Collections.singletonList("<stackSize>");

        int maxStackAmount = spawnerStackSettings.getMaxStackSize();
        return Arrays.asList(String.valueOf(maxStackAmount), String.valueOf(maxStackAmount / 2), String.valueOf(maxStackAmount / 4), "<stackSize>");
    }

}
