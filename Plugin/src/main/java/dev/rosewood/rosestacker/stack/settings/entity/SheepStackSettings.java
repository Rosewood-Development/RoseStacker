package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;

public class SheepStackSettings extends EntityStackSettings {

    private final boolean dontStackIfSheared;
    private final boolean dontStackIfDifferentShearState;
    private final boolean shearAllSheepInStack;
    private final int percentageOfWoolToRegrowPerGrassEaten;

    public SheepStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfSheared = this.settingsConfiguration.getBoolean("dont-stack-if-sheared");
        this.dontStackIfDifferentShearState = this.settingsConfiguration.getBoolean("dont-stack-if-different-shear-state");
        this.shearAllSheepInStack =  this.settingsConfiguration.getBoolean("shear-all-sheep-in-stack");
        this.percentageOfWoolToRegrowPerGrassEaten = this.settingsConfiguration.getInt("percentage-of-wool-to-regrow-per-grass-eaten");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Sheep sheep1 = (Sheep) stack1.getEntity();
        Sheep sheep2 = (Sheep) stack2.getEntity();

        if (this.dontStackIfSheared && (sheep1.isSheared() || sheep2.isSheared()))
            return EntityStackComparisonResult.SHEARED;

        if (this.dontStackIfDifferentShearState && (sheep1.isSheared() != sheep2.isSheared()))
            return EntityStackComparisonResult.SHEARED_STATE_DIFFERENT;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-sheared", false);
        this.setIfNotExists("dont-stack-if-different-shear-state", false);
        this.setIfNotExists("shear-all-sheep-in-stack", true);
        this.setIfNotExists("percentage-of-wool-to-regrow-per-grass-eaten", 25);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SHEEP;
    }

    public boolean shouldShearAllSheepInStack() {
        return this.shearAllSheepInStack;
    }

    public int getPercentageOfWoolToRegrowPerGrassEaten() {
        return this.percentageOfWoolToRegrowPerGrassEaten;
    }

}
