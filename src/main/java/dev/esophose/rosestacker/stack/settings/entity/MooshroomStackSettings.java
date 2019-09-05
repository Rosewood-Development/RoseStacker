package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;

public class MooshroomStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentType;

    public MooshroomStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);

        this.dontStackIfDifferentType = entitySettingsConfiguration.getBoolean("dont-stack-if-different-type");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        MushroomCow mooshroom1 = (MushroomCow) stack1.getEntity();
        MushroomCow mooshroom2 = (MushroomCow) stack2.getEntity();

        return !this.dontStackIfDifferentType || (mooshroom1.getVariant() == mooshroom2.getVariant());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-type", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MUSHROOM_COW;
    }

}
