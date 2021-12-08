package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;

public class MushroomCowStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentType;
    private final boolean dropAdditionalMushroomsPerCowInStack;
    private final int extraMushroomsPerCowInStack;

    public MushroomCowStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
        this.dropAdditionalMushroomsPerCowInStack = this.settingsConfiguration.getBoolean("drop-additional-mushrooms-for-each-cow-in-stack");
        this.extraMushroomsPerCowInStack = this.settingsConfiguration.getInt("extra-mushrooms-per-cow-in-stack");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        MushroomCow mushroomCow1 = (MushroomCow) stack1.getEntity();
        MushroomCow mushroomCow2 = (MushroomCow) stack2.getEntity();

        if (this.dontStackIfDifferentType && mushroomCow1.getVariant() != mushroomCow2.getVariant())
            return EntityStackComparisonResult.DIFFERENT_TYPES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
        this.setIfNotExists("drop-additional-mushrooms-for-each-cow-in-stack", true);
        this.setIfNotExists("extra-mushrooms-per-cow-in-stack", 5);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MUSHROOM_COW;
    }

    public boolean shouldDropAdditionalMushroomsPerCowInStack() {
        return this.dropAdditionalMushroomsPerCowInStack;
    }

    public int getExtraMushroomsPerCowInStack() {
        return this.extraMushroomsPerCowInStack;
    }

}
