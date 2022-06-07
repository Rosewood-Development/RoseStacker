package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;

public class GoatStackSettings extends EntityStackSettings {

    private final boolean dontStackIfScreaming;
    private final boolean dontStackIfDifferentHorns;

    public GoatStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfScreaming = this.settingsConfiguration.getBoolean("dont-stack-if-screaming");
        this.dontStackIfDifferentHorns = this.settingsConfiguration.getBoolean("dont-stack-if-different-horns");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Goat goat1 = (Goat) stack1.getEntity();
        Goat goat2 = (Goat) stack2.getEntity();

        if (this.dontStackIfScreaming && (goat1.isScreaming() || goat2.isScreaming()))
            return EntityStackComparisonResult.SCREAMING;

        if (NMSUtil.getVersionNumber() >= 19 && this.dontStackIfDifferentHorns && (goat1.hasLeftHorn() != goat2.hasLeftHorn() || goat1.hasRightHorn() != goat2.hasRightHorn()))
            return EntityStackComparisonResult.DIFFERENT_HORNS;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-screaming", false);
        if (NMSUtil.getVersionNumber() >= 19)
            this.setIfNotExists("dont-stack-if-different-horns", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.GOAT;
    }

}
