package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PiglinBrute;

public class PiglinBruteStackSettings extends EntityStackSettings {

    private final boolean dontStackIfConverting;
    private final boolean dontStackIfDifferentAge;
    private final boolean dontStackIfImmuneToZombification;

    public PiglinBruteStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfConverting = this.settingsConfiguration.getBoolean("dont-stack-if-converting");
        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
        this.dontStackIfImmuneToZombification = this.settingsConfiguration.getBoolean("dont-stack-if-immune-to-zombification");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PiglinBrute piglinBrute1 = (PiglinBrute) stack1.getEntity();
        PiglinBrute piglinBrute2 = (PiglinBrute) stack2.getEntity();

        if (this.dontStackIfConverting && (piglinBrute1.isConverting() || piglinBrute2.isConverting()))
            return EntityStackComparisonResult.CONVERTING;

        if (this.dontStackIfDifferentAge && (piglinBrute1.isBaby() != piglinBrute2.isBaby()))
            return EntityStackComparisonResult.DIFFERENT_AGES;

        if (this.dontStackIfImmuneToZombification && (piglinBrute1.isImmuneToZombification() || piglinBrute2.isImmuneToZombification()))
            return EntityStackComparisonResult.IMMUNE_TO_ZOMBIFICATION;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-converting", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
        this.setIfNotExists("dont-stack-if-immune-to-zombification", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PIGLIN_BRUTE;
    }

}
