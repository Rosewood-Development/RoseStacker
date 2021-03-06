package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;

public class CatStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentType;
    private final boolean dontStackIfDifferentCollarColor;

    public CatStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
        this.dontStackIfDifferentCollarColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-collar-color");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Cat cat1 = (Cat) stack1.getEntity();
        Cat cat2 = (Cat) stack2.getEntity();

        if (this.dontStackIfDifferentType && cat1.getCatType() != cat2.getCatType())
            return EntityStackComparisonResult.DIFFERENT_TYPES;

        if (this.dontStackIfDifferentCollarColor && cat1.getCollarColor() != cat2.getCollarColor())
            return EntityStackComparisonResult.DIFFERENT_COLLAR_COLORS;

        return EntityStackComparisonResult.CAN_STACK;
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
