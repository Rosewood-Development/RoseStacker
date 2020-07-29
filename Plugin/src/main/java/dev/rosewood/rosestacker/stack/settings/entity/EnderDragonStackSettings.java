package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class EnderDragonStackSettings extends EntityStackSettings {

    public EnderDragonStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        return true;
    }

    @Override
    protected void setDefaultsInternal() {

    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENDER_DRAGON;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return null;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Collections.emptyList();
    }

}
