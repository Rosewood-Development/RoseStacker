package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

public class ZombieStackSettings extends EntityStackSettings {

    protected boolean dontStackIfConverting;

    public ZombieStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfConverting = this.settingsConfiguration.getBoolean("dont-stack-if-converting");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Zombie zombie1 = (Zombie) stack1.getEntity();
        Zombie zombie2 = (Zombie) stack2.getEntity();

        if (this.dontStackIfConverting && (zombie1.isConverting() || zombie2.isConverting()))
            return EntityStackComparisonResult.CONVERTING;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-converting", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOMBIE;
    }

}
