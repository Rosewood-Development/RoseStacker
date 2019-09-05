package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class CaveSpiderStackSettings extends EntityStackSettings {

    public CaveSpiderStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);
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
        return EntityType.CAVE_SPIDER;
    }

}
