package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.EntityType;

public class AxolotlStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentColor;
    private final boolean dontStackIfPlayingDead;

    public AxolotlStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfDifferentColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-color");
        this.dontStackIfPlayingDead = this.settingsConfiguration.getBoolean("dont-stack-if-playing-dead");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Axolotl axolotl1 = (Axolotl) stack1.getEntity();
        Axolotl axolotl2 = (Axolotl) stack2.getEntity();

        if (this.dontStackIfDifferentColor && axolotl1.getVariant() != axolotl2.getVariant())
            return EntityStackComparisonResult.DIFFERENT_COLORS;

        if (this.dontStackIfPlayingDead && (axolotl1.isPlayingDead() || axolotl2.isPlayingDead()))
            return EntityStackComparisonResult.PLAYING_DEAD;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-color", false);
        this.setIfNotExists("dont-stack-if-playing-dead", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.AXOLOTL;
    }

}
