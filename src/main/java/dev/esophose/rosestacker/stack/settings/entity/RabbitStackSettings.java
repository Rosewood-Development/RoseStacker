package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Rabbit;

public class RabbitStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentType;

    public RabbitStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);

        this.dontStackIfDifferentType = entitySettingsConfiguration.getBoolean("dont-stack-if-different-type");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Rabbit rabbit1 = (Rabbit) stack1.getEntity();
        Rabbit rabbit2 = (Rabbit) stack2.getEntity();

        return !this.dontStackIfDifferentType || rabbit1.getRabbitType() == rabbit2.getRabbitType();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.RABBIT;
    }

}
