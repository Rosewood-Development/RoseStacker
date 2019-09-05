package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;

public class ParrotStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentType;

    public ParrotStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);

        this.dontStackIfDifferentType = entitySettingsConfiguration.getBoolean("dont-stack-if-different-type");
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

}
