package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;

public class ParrotStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentType;

    public ParrotStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Parrot parrot1 = (Parrot) stack1.getEntity();
        Parrot parrot2 = (Parrot) stack2.getEntity();

        if (this.dontStackIfDifferentType && parrot1.getVariant() != parrot2.getVariant())
            return EntityStackComparisonResult.DIFFERENT_TYPES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
    }

    @Override
    public boolean isFlyingMob() {
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PARROT;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.PARROT_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "block:grass_block,acacia_leaves,birch_leaves,dark_oak_leaves,jungle_leaves,oak_leaves,spruce_leaves",
                "lightness"
        );
    }

}
