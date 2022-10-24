package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.manager.ConversionManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StackPluginArgumentHandler extends RoseCommandArgumentHandler<StackPlugin> {

    public StackPluginArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackPlugin.class);
    }

    @Override
    protected StackPlugin handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();
        return Arrays.stream(StackPlugin.values())
                .filter(x -> x.name().equalsIgnoreCase(input))
                .findFirst()
                .orElseThrow(() -> new HandledArgumentException("argument-handler-stackplugin", StringPlaceholders.single("input", input)));
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return this.rosePlugin.getManager(ConversionManager.class).getEnabledConverters().stream()
                .map(StackPlugin::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

}
