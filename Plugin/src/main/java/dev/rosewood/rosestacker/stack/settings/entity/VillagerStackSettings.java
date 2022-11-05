package dev.rosewood.rosestacker.stack.settings.entity;

import com.google.gson.JsonObject;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.List;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class VillagerStackSettings extends EntityStackSettings {

    private static final List<String> UNPROFESSIONED_VALUE_NAMES = List.of("NONE", "NITWIT");

    private final boolean dontStackIfProfessioned;
    private final boolean dontStackIfDifferentProfession;
    private final boolean dontStackIfDifferentType;
    private final boolean dontStackIfDifferentLevel;
    private final boolean dontStackIfHasJob;

    public VillagerStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration, JsonObject jsonObject) {
        super(entitySettingsFileConfiguration, jsonObject);

        this.dontStackIfProfessioned = this.settingsConfiguration.getBoolean("dont-stack-if-professioned");
        this.dontStackIfDifferentProfession = this.settingsConfiguration.getBoolean("dont-stack-if-different-profession");
        this.dontStackIfDifferentType = this.settingsConfiguration.getBoolean("dont-stack-if-different-type");
        this.dontStackIfDifferentLevel = this.settingsConfiguration.getBoolean("dont-stack-if-different-level");
        this.dontStackIfHasJob = this.settingsConfiguration.getBoolean("dont-stack-if-has-job");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Villager villager1 = (Villager) stack1.getEntity();
        Villager villager2 = (Villager) stack2.getEntity();

        if (this.dontStackIfProfessioned && (!UNPROFESSIONED_VALUE_NAMES.contains(villager1.getProfession().name()) || !UNPROFESSIONED_VALUE_NAMES.contains(villager2.getProfession().name())))
            return EntityStackComparisonResult.PROFESSIONED;

        if (this.dontStackIfDifferentProfession && villager1.getProfession() != villager2.getProfession())
            return EntityStackComparisonResult.DIFFERENT_PROFESSIONS;

        if (this.dontStackIfDifferentType && villager1.getType() != villager2.getType())
            return EntityStackComparisonResult.DIFFERENT_TYPES;

        if (this.dontStackIfDifferentLevel && villager1.getVillagerLevel() != villager2.getVillagerLevel())
            return EntityStackComparisonResult.DIFFERENT_LEVELS;

        if (this.dontStackIfHasJob && (villager1.getProfession() != Villager.Profession.NONE || villager2.getProfession() != Villager.Profession.NONE))
            return EntityStackComparisonResult.HAS_JOB;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-different-profession", false);
        this.setIfNotExists("dont-stack-if-different-type", false);
        this.setIfNotExists("dont-stack-if-different-level", false);
        this.setIfNotExists("dont-stack-if-has-job", false);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.VILLAGER;
    }

}
