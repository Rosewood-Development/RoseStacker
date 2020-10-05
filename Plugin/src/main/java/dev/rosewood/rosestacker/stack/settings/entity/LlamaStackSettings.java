package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama;

public class LlamaStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentDecor;
    private final boolean dontStackIfDifferentColor;

    public LlamaStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentDecor = this.settingsConfiguration.getBoolean("dont-stack-if-different-decor");
        this.dontStackIfDifferentColor = this.settingsConfiguration.getBoolean("dont-stack-if-different-color");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Llama llama1 = (Llama) stack1.getEntity();
        Llama llama2 = (Llama) stack2.getEntity();

        if (this.dontStackIfDifferentDecor && llama1.getInventory().getDecor() != llama2.getInventory().getDecor())
            return EntityStackComparisonResult.DIFFERENT_DECORS;

        if (this.dontStackIfDifferentColor && llama1.getColor() != llama2.getColor())
            return EntityStackComparisonResult.DIFFERENT_COLORS;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-decor", false);
        this.setIfNotExists("dont-stack-if-different-color", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.LLAMA;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.LLAMA_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return ConditionTags.ANIMAL_TAGS;
    }

}
