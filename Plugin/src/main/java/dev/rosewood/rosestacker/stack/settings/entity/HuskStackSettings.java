package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.spawner.ConditionTags;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;

public class HuskStackSettings extends ZombieStackSettings {

    public HuskStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Husk husk1 = (Husk) stack1.getEntity();
        Husk husk2 = (Husk) stack2.getEntity();

        if (this.dontStackIfConverting && (husk1.isConverting() || husk2.isConverting()))
            return EntityStackComparisonResult.CONVERTING;

        if (this.dontStackIfDifferentAge && husk1.isBaby() != husk2.isBaby())
            return EntityStackComparisonResult.DIFFERENT_AGES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HUSK;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.HUSK_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return ConditionTags.MONSTER_TAGS;
    }

}
