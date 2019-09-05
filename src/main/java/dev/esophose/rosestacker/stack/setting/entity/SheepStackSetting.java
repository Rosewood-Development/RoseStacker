package dev.esophose.rosestacker.stack.setting.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.setting.EntityStackSetting;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;

public class SheepStackSetting extends EntityStackSetting {

    private boolean dontStackIfSheared;
    private boolean onlyStackSimilarColors;

    public SheepStackSetting(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);

        this.dontStackIfSheared = entitySettingsConfiguration.getBoolean("dont-stack-if-sheared");
        this.onlyStackSimilarColors = entitySettingsConfiguration.getBoolean("only-stack-similar-colors");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Sheep sheep1 = (Sheep) stack1.getEntity();
        Sheep sheep2 = (Sheep) stack2.getEntity();

        if (dontStackIfSheared && (sheep1.isSheared() || sheep2.isSheared()))
            return false;

        return !onlyStackSimilarColors || (sheep1.getColor() == sheep2.getColor());
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-sheared", false);
        this.setIfNotExists("only-stack-similar-colors", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SHEEP;
    }

}
