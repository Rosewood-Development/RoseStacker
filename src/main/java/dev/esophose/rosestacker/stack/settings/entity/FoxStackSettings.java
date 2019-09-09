package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;

public class FoxStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentType;

    public FoxStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Fox fox1 = (Fox) stack1.getEntity();
        Fox fox2 = (Fox) stack2.getEntity();

        return !this.dontStackIfDifferentType || fox1.getFoxType() == fox2.getFoxType();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FOX;
    }

}
