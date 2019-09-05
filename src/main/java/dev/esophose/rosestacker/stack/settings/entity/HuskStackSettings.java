package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;

public class HuskStackSettings extends ZombieStackSettings {

    public HuskStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Husk husk1 = (Husk) stack1.getEntity();
        Husk husk2 = (Husk) stack2.getEntity();

        if (this.dontStackIfConverting && (husk1.isConverting() || husk2.isConverting()))
            return false;

        return !this.dontStackIfDifferentAge || husk1.isBaby() != husk2.isBaby();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HUSK;
    }

}
