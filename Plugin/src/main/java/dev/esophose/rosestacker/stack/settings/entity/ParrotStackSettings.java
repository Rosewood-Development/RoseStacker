package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;

public class ParrotStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentType;

    public ParrotStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Parrot parrot1 = (Parrot) stack1.getEntity();
        Parrot parrot2 = (Parrot) stack2.getEntity();

        return !this.dontStackIfDifferentType || (parrot1.getVariant() == parrot2.getVariant());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PARROT;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.PARROT_SPAWN_EGG;
    }

}
