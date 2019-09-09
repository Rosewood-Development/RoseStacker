package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;

public class IronGolemStackSettings extends EntityStackSettings {

    private boolean dontStackIfPlayerCreated;

    public IronGolemStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfPlayerCreated = this.settingsConfiguration.getBoolean("dont-stack-if-player-created");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        IronGolem ironGolem1 = (IronGolem) stack1.getEntity();
        IronGolem ironGolem2 = (IronGolem) stack2.getEntity();

        return !this.dontStackIfPlayerCreated || (!ironGolem1.isPlayerCreated() && !ironGolem2.isPlayerCreated());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-player-created", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.IRON_GOLEM;
    }

}
