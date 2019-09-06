package dev.esophose.rosestacker.stack.settings.entity;

import dev.esophose.rosestacker.config.CommentedFileConfiguration;
import org.bukkit.entity.EntityType;

public class TraderLlamaStackSettings extends LlamaStackSettings {

    public TraderLlamaStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TRADER_LLAMA;
    }

}
