package dev.esophose.sparkstacker.stack.settings.entity;

import dev.esophose.sparkstacker.config.CommentedFileConfiguration;
import dev.esophose.sparkstacker.stack.StackedEntity;
import dev.esophose.sparkstacker.stack.settings.EntityStackSettings;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;

public class PhantomStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentSize;

    public PhantomStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentSize = this.settingsConfiguration.getBoolean("dont-stack-if-different-size");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Phantom phantom1 = (Phantom) stack1.getEntity();
        Phantom phantom2 = (Phantom) stack2.getEntity();

        return !this.dontStackIfDifferentSize || phantom1.getSize() == phantom2.getSize();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-size", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PHANTOM;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.PHANTOM_SPAWN_EGG;
    }

}
