package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zoglin;

public class ZoglinStackSettings extends EntityStackSettings {

    private final boolean dontStackIfDifferentAge;

    public ZoglinStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentAge = this.settingsConfiguration.getBoolean("dont-stack-if-different-age");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Zoglin zoglin1 = (Zoglin) stack1.getEntity();
        Zoglin zoglin2 = (Zoglin) stack2.getEntity();

        if (this.dontStackIfDifferentAge && zoglin1.isBaby() != zoglin2.isBaby())
            return EntityStackComparisonResult.DIFFERENT_AGES;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-age", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ZOGLIN;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.ZOGLIN_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Arrays.asList(
                "darkness",
                "block-exception:nether_wart_block"
        );
    }

}
