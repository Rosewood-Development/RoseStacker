package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;

public class SlimeStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentSize;

    public SlimeStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentSize = this.settingsConfiguration.getBoolean("dont-stack-if-different-size");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Slime slime1 = (Slime) stack1.getEntity();
        Slime slime2 = (Slime) stack2.getEntity();

        if (this.dontStackIfDifferentSize && slime1.getSize() != slime2.getSize())
            return EntityStackComparisonResult.DIFFERENT_SIZES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-size", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SLIME;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.SLIME_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "below-y-axis:40",
                "lightness"
        );
    }

}
