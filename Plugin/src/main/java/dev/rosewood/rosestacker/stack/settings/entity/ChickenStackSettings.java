package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;

public class ChickenStackSettings extends EntityStackSettings {

    private final boolean multiplyEggDropsByStackSize;

    public ChickenStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.multiplyEggDropsByStackSize = this.settingsConfiguration.getBoolean("multiply-egg-drops-by-stack-size");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("multiply-egg-drops-by-stack-size", true);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.CHICKEN;
    }

    public boolean shouldMultiplyEggDropsByStackSize() {
        return this.multiplyEggDropsByStackSize;
    }

}
