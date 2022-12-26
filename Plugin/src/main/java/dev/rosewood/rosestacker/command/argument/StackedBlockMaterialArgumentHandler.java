package dev.rosewood.rosestacker.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;

public class StackedBlockMaterialArgumentHandler extends RoseCommandArgumentHandler<StackedBlockMaterialArgumentHandler.StackedBlockMaterial> {

    public StackedBlockMaterialArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackedBlockMaterial.class);
    }

    @Override
    protected StackedBlockMaterial handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();
        return Arrays.stream(Material.values())
                .filter(x -> x.name().equalsIgnoreCase(input))
                .map(StackedBlockMaterial::new)
                .findFirst()
                .orElseThrow(() -> new HandledArgumentException("argument-handler-material", StringPlaceholders.single("input", input)));
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();
        return this.rosePlugin.getManager(StackSettingManager.class).getStackableBlockTypes().stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public record StackedBlockMaterial(Material material) { }

}
