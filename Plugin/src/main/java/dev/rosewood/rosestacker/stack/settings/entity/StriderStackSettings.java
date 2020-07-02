package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Zombie;

public class StriderStackSettings extends EntityStackSettings {

    private boolean dontStackIfShivering;
    private boolean dontStackIfSaddled;

    public StriderStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfShivering = this.settingsConfiguration.getBoolean("dont-stack-if-shivering");
        this.dontStackIfSaddled = this.settingsConfiguration.getBoolean("dont-stack-if-saddled");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Strider strider1 = (Strider) stack1.getEntity();
        Strider strider2 = (Strider) stack2.getEntity();

        if (this.dontStackIfShivering && (strider1.isShivering() || strider2.isShivering()))
            return false;

        return !this.dontStackIfSaddled || (!strider1.hasSaddle() && !strider2.hasSaddle());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-shivering", false);
        this.setIfNotExists("dont-stack-if-saddled", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.STRIDER;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.STRIDER_SPAWN_EGG;
    }

}
