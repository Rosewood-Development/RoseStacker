package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import dev.esophose.rosestacker.utils.ClassUtils;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackSettingManager extends Manager {

    private Map<EntityType, EntityStackSettings> entitySettings;

    public StackSettingManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.entitySettings = new HashMap<>();
    }

    @Override
    public void reload() {
        // Load block settings
        // TODO

        // Load entity settings
        this.entitySettings.clear();

        File file = new File(this.roseStacker.getDataFolder(), "entity_settings.yml");
        CommentedFileConfiguration entitySettingsConfiguration = CommentedFileConfiguration.loadConfiguration(this.roseStacker, file);

        try {
            List<Class<EntityStackSettings>> classes = ClassUtils.getClassesOf(this.roseStacker, "dev.esophose.rosestacker.stack.settings.entity", EntityStackSettings.class);
            for (Class<EntityStackSettings> clazz : classes) {
                EntityStackSettings entityStackSetting = clazz.getConstructor(CommentedFileConfiguration.class).newInstance(entitySettingsConfiguration);
                this.entitySettings.put(entityStackSetting.getEntityType(), entityStackSetting);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        entitySettingsConfiguration.save(true);

        // Load item settings
        // TODO

        // Load spawner settings
        // TODO
    }

    @Override
    public void disable() {

    }

    public boolean canEntitiesBeStacked(StackedEntity entity1, StackedEntity entity2) {
        EntityStackSettings stackSettings = this.entitySettings.get(entity1.getEntity().getType());
        return stackSettings.canStackWith(entity1, entity2);
    }

    public EntityStackSettings getEntityStackSettings(EntityType entityType) {
        return this.entitySettings.get(entityType);
    }

}
