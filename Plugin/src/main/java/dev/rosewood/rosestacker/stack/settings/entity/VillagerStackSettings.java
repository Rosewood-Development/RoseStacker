package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosestacker.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.nms.NMSUtil;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class VillagerStackSettings extends EntityStackSettings {

    private boolean dontStackIfDifferentProfession;
    private boolean dontStackIfDifferentType;
    private boolean dontStackIfDifferentLevel;

    public VillagerStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfDifferentProfession = this.settingsConfiguration.getBoolean("dont-stack-if-different-profession");
        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
        if (NMSUtil.getVersionNumber() >= 14)
            this.dontStackIfDifferentLevel = this.settingsConfiguration.getBoolean("dont-stack-if-different-level");
    }

    @Override
    protected boolean canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Villager villager1 = (Villager) stack1.getEntity();
        Villager villager2 = (Villager) stack2.getEntity();

        if (this.dontStackIfDifferentProfession && villager1.getProfession() != villager2.getProfession())
            return false;

        if (this.dontStackIfDifferentType && villager1.getType() != villager2.getType())
            return false;

        if (NMSUtil.getVersionNumber() <= 13)
            return true;

        return !this.dontStackIfDifferentLevel || villager1.getVillagerLevel() == villager2.getVillagerLevel();
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-profession", false);
        this.setIfNotExists("dont-stack-if-different-type", false);
        if (NMSUtil.getVersionNumber() >= 14)
            this.setIfNotExists("dont-stack-if-different-level", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.VILLAGER;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.VILLAGER_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Collections.singletonList("on-ground");
    }

}
