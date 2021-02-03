package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hoglin;

public class HoglinStackSettings extends EntityStackSettings {

    private final boolean dontStackIfUnhuntable;

    public HoglinStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfUnhuntable = this.settingsConfiguration.getBoolean("dont-stack-if-unhuntable");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Hoglin hoglin1 = (Hoglin) stack1.getEntity();
        Hoglin hoglin2 = (Hoglin) stack2.getEntity();

        if (this.dontStackIfUnhuntable && (!hoglin1.isAbleToBeHunted() || hoglin2.isAbleToBeHunted()))
            return EntityStackComparisonResult.UNHUNTABLE;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-unhuntable", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HOGLIN;
    }

}
