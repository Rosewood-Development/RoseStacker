package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

public class ZombieStackSettings extends EntityStackSettings {

    protected boolean dontStackIfConverting;
    protected boolean dontStackIfDifferentAge;

    public ZombieStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfConverting = this.settingsConfiguration.getBoolean("dont-stack-if-converting");
        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Zombie zombie1 = (Zombie) stack1.getEntity();
        Zombie zombie2 = (Zombie) stack2.getEntity();

        if (this.dontStackIfConverting && (zombie1.isConverting() || zombie2.isConverting()))
            return false;

        return !this.dontStackIfDifferentAge || zombie1.isBaby() == zombie2.isBaby();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-converting", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOMBIE;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.ZOMBIE_SPAWN_EGG;
    }

}
