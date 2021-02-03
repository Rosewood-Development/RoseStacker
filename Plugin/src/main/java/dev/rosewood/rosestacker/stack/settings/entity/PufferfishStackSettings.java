package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PufferFish;

public class PufferfishStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentInflation;

    public PufferfishStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDifferentInflation = this.settingsConfiguration.getBoolean("dont-stack-if-different-inflation");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PufferFish pufferFish1 = (PufferFish) stack1.getEntity();
        PufferFish pufferFish2 = (PufferFish) stack2.getEntity();

        if (this.dontStackIfDifferentInflation && pufferFish1.getPuffState() != pufferFish2.getPuffState())
            return EntityStackComparisonResult.DIFFERENT_INFLATIONS;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-inflation", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PUFFERFISH;
    }

}
