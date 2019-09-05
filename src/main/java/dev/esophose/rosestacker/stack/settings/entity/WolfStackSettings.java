package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.ZombieHorse;

public class WolfStackSettings extends EntityStackSettings {

    private boolean dontStackIfAngry;
    private boolean dontStackIfDifferentCollarColor;

    public WolfStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);

        this.dontStackIfAngry = entitySettingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfDifferentCollarColor = entitySettingsConfiguration.getBoolean("dont-stack-if-different-collar-color");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Wolf wolf1 = (Wolf) stack1.getEntity();
        Wolf wolf2 = (Wolf) stack2.getEntity();

        if (this.dontStackIfAngry && (wolf1.isAngry() || wolf2.isAngry()))
            return false;

        return this.dontStackIfDifferentCollarColor || wolf1.getCollarColor() == wolf2.getCollarColor();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-different-collar-color", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.WOLF;
    }

}
