package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;

public class RabbitStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentType;

    public RabbitStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Rabbit rabbit1 = (Rabbit) stack1.getEntity();
        Rabbit rabbit2 = (Rabbit) stack2.getEntity();

        if (this.dontStackIfDifferentType && rabbit1.getRabbitType() != rabbit2.getRabbitType())
            return EntityStackComparisonResult.DIFFERENT_TYPES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.RABBIT;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.RABBIT_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "block:grass_block,snow,sand",
                "lightness"
        );
    }

}
