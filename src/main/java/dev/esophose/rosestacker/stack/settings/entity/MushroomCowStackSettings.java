package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;

public class MushroomCowStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentType;

    public MushroomCowStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentType = this.entitySettingsConfiguration.getBoolean("dont-stack-if-different-type");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        MushroomCow mushroomCow1 = (MushroomCow) stack1.getEntity();
        MushroomCow mushroomCow2 = (MushroomCow) stack2.getEntity();

        return !this.dontStackIfDifferentType || (mushroomCow1.getVariant() == mushroomCow2.getVariant());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MUSHROOM_COW;
    }

}
