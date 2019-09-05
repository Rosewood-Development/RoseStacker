package dev.esophose.rosestacker.manager;

import com.google.common.reflect.ClassPath;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.stack.setting.EntityStackSetting;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class StackSettingManager extends Manager {

    private YamlConfiguration entitySettingsConfiguration;
    private YamlConfiguration itemSettingsConfiguration;
    private YamlConfiguration spawnerSettingsConfiguration;
    private YamlConfiguration blockSettingsConfiguration;

    public Map<EntityType, EntityStackSetting> entitySettings;

    public StackSettingManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.entitySettings = new HashMap<>();
    }

    @Override
    public void reload() {
        this.entitySettings.clear();

        // Load all entity settings using a ClassLoader instead of adding them all to the map manually
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            for (ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith("dev.esophose.rosestacker.stack.setting.entity")) {
                    Class<?> clazz = info.load();
                    Object obj = clazz.getConstructor(FileConfiguration.class).newInstance(this.entitySettingsConfiguration);
                    EntityStackSetting entityStackSetting = (EntityStackSetting) obj;
                    this.entitySettings.put(entityStackSetting.getEntityType(), entityStackSetting);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disable() {

    }

}
