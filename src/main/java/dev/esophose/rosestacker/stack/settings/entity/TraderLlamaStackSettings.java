package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.settings.EntityStackSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class TraderLlamaStackSettings extends LlamaStackSettings {

    public TraderLlamaStackSettings(YamlConfiguration entitySettingsConfiguration) {
        super(entitySettingsConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TRADER_LLAMA;
    }

}
