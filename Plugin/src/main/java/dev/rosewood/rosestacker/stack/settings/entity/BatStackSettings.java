package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;

public class BatStackSettings extends EntityStackSettings {

    private final boolean dontStackIfSleeping;

    public BatStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfSleeping = this.settingsConfiguration.getBoolean("dont-stack-if-sleeping");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Bat bat1 = (Bat) stack1.getEntity();
        Bat bat2 = (Bat) stack2.getEntity();

        if (this.dontStackIfSleeping && (!bat1.isAwake() || !bat2.isAwake()))
            return EntityStackComparisonResult.SLEEPING;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-sleeping", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.BAT;
    }

}
