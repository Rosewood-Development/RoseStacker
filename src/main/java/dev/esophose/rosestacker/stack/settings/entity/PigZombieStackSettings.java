package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;

public class PigZombieStackSettings extends EntityStackSettings {

    protected boolean dontStackIfAngry;
    protected boolean dontStackIfDifferentAge;

    public PigZombieStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);

        this.dontStackIfAngry = entitySettingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfDifferentAge = entitySettingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        PigZombie pigZombie1 = (PigZombie) stack1.getEntity();
        PigZombie pigZombie2 = (PigZombie) stack2.getEntity();

        if (this.dontStackIfAngry && (pigZombie1.isAngry() || pigZombie2.isAngry()))
            return false;

        return !this.dontStackIfDifferentAge || pigZombie1.isBaby() != pigZombie2.isBaby();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PIG_ZOMBIE;
    }

}
