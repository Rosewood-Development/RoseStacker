package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ZombieVillager;

public class ZombieHorseStackSettings extends EntityStackSettings {

    public ZombieHorseStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        return true;
    }

    @Override
    protected void setDefaultsInternal() {

    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOMBIE_HORSE;
    }

}
