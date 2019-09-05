package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;

public class SheepStackSettings extends EntityStackSettings {

    private boolean dontStackIfSheared;

    public SheepStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);

        this.dontStackIfSheared = entitySettingsConfiguration.getBoolean("dont-stack-if-sheared");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Sheep sheep1 = (Sheep) stack1.getEntity();
        Sheep sheep2 = (Sheep) stack2.getEntity();

        return !this.dontStackIfSheared || (!sheep1.isSheared() && !sheep2.isSheared());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-sheared", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SHEEP;
    }

}
