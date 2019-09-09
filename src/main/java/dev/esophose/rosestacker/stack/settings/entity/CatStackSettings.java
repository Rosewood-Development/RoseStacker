package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;

public class CatStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentType;
    private boolean dontStackIfDifferentCollarColor;

    public CatStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
        this.dontStackIfDifferentCollarColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-collar-color");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Cat cat1 = (Cat) stack1.getEntity();
        Cat cat2 = (Cat) stack2.getEntity();

        if (this.dontStackIfDifferentType && cat1.getCatType() != cat2.getCatType())
            return false;

        return !this.dontStackIfDifferentCollarColor || cat1.getCollarColor() == cat2.getCollarColor();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
        this.setIfNotExists("dont-stack-if-different-collar-color", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.CAT;
    }

}
