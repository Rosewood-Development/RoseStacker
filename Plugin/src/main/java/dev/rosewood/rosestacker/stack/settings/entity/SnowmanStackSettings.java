package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowman;

public class SnowmanStackSettings extends EntityStackSettings {

    private boolean dontStackIfNoPumpkin;

    public SnowmanStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfNoPumpkin = this.settingsConfiguration.getBoolean("dont-stack-if-no-pumpkin");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Snowman snowman1 = (Snowman) stack1.getEntity();
        Snowman snowman2 = (Snowman) stack2.getEntity();

        return !this.dontStackIfNoPumpkin || (!snowman1.isDerp() && !snowman2.isDerp());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-no-pumpkin", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SNOWMAN;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return null;
    }

}
