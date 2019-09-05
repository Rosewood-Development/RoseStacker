package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import dev.esophose.rosestacker.utils.ClassUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackSettingManager extends Manager {

    private YamlConfiguration blockSettingsConfiguration;
    private YamlConfiguration entitySettingsConfiguration;
    private YamlConfiguration itemSettingsConfiguration;
    private YamlConfiguration spawnerSettingsConfiguration;

    public Map<EntityType, EntityStackSettings> entitySettings;

    public StackSettingManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.entitySettings = new HashMap<>();
    }

    @Override
    public void reload() {
        this.entitySettings.clear();

        File file = new File(this.roseStacker.getDataFolder(), "entity_settings.yml");
        if (this.entitySettingsConfiguration == null) {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.entitySettingsConfiguration = YamlConfiguration.loadConfiguration(file);
        }

        try {
            List<Class<EntityStackSettings>> classes = ClassUtils.getClassesOf(this.roseStacker, "dev.esophose.rosestacker.stack.settings.entity", EntityStackSettings.class);
            for (Class<EntityStackSettings> clazz : classes) {
                EntityStackSettings entityStackSetting = clazz.getConstructor(YamlConfiguration.class).newInstance(this.entitySettingsConfiguration);
                this.entitySettings.put(entityStackSetting.getEntityType(), entityStackSetting);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.entitySettingsConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
