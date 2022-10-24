package dev.rosewood.rosestacker.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.manager.ConversionManager;
import dev.rosewood.rosestacker.manager.LocaleManager;
import java.util.List;

public class ConvertCommand extends RoseCommand {

    public ConvertCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, StackPlugin plugin) {
        ConversionManager conversionManager = this.rosePlugin.getManager(ConversionManager.class);
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);

        if (conversionManager.convert(plugin)) {
            localeManager.sendMessage(context.getSender(), "command-convert-converted", StringPlaceholders.single("plugin", plugin.name()));
        } else {
            localeManager.sendMessage(context.getSender(), "command-convert-failed", StringPlaceholders.single("plugin", plugin.name()));
        }
    }

    @Override
    protected String getDefaultName() {
        return "convert";
    }

    @Override
    public String getDescriptionKey() {
        return "command-convert-description";
    }

    @Override
    public String getRequiredPermission() {
        return "rosestacker.convert";
    }

}
