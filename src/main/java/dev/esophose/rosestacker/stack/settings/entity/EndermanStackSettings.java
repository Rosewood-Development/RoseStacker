package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;

public class EndermanStackSettings extends EntityStackSettings {

    private boolean dontStackIfHoldingBlock;

    public EndermanStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfHoldingBlock = this.settingsConfiguration.getBoolean("dont-stack-if-holding-block");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Enderman enderman1 = (Enderman) stack1.getEntity();
        Enderman enderman2 = (Enderman) stack2.getEntity();

        return !this.dontStackIfHoldingBlock || (enderman1.getCarriedBlock() == null && enderman2.getCarriedBlock() == null);
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-holding-block", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENDERMAN;
    }

}
