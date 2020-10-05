package dev.rosewood.rosestacker.stack.settings.entity;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class BeeStackSettings extends EntityStackSettings {

    private final boolean dontStackIfAngry;
    private final boolean dontStackIfHasHive;
    private final boolean dontStackIfDifferentHives;
    private final boolean dontStackIfStung;
    private final boolean dontStackIfHasFlower;
    private final boolean dontStackIfHasNectar;

    public BeeStackSettings(CommentedFileConfiguration entitySettingsFileConfiguration) {
        super(entitySettingsFileConfiguration);

        this.dontStackIfAngry = this.settingsConfiguration.getBoolean("dont-stack-if-angry");
        this.dontStackIfHasHive = this.settingsConfiguration.getBoolean("dont-stack-if-has-hive");
        this.dontStackIfDifferentHives = this.settingsConfiguration.getBoolean("dont-stack-if-different-hives");
        this.dontStackIfStung = this.settingsConfiguration.getBoolean("dont-stack-if-stung");
        this.dontStackIfHasFlower = this.settingsConfiguration.getBoolean("dont-stack-if-has-flower");
        this.dontStackIfHasNectar = this.settingsConfiguration.getBoolean("dont-stack-if-has-nectar");
    }

    @Override
    protected EntityStackComparisonResult canStackWithInternal(StackedEntity stack1, StackedEntity stack2) {
        Bee bee1 = (Bee) stack1.getEntity();
        Bee bee2 = (Bee) stack2.getEntity();

        if (this.dontStackIfAngry && (bee1.getAnger() > 0 || bee2.getAnger() > 0))
            return EntityStackComparisonResult.ANGRY;

        if (this.dontStackIfHasHive && (bee1.getHive() != null || bee2.getHive() != null))
            return EntityStackComparisonResult.HAS_HIVE;

        if (this.dontStackIfDifferentHives && (bee1.getHive() != null && bee2.getHive() != null && !bee1.getHive().equals(bee2.getHive())))
            return EntityStackComparisonResult.DIFFERENT_HIVES;

        if (this.dontStackIfStung && (bee1.hasStung() || bee2.hasStung()))
            return EntityStackComparisonResult.HAS_STUNG;

        if (this.dontStackIfHasFlower && (bee1.getFlower() != null || bee2.getFlower() != null))
            return EntityStackComparisonResult.HAS_FLOWER;

        if (this.dontStackIfHasNectar && (bee1.hasNectar() || bee2.hasNectar()))
            return EntityStackComparisonResult.HAS_NECTAR;

        return EntityStackComparisonResult.CAN_STACK;
    }

    @Override
    protected void setDefaultsInternal() {
        this.setIfNotExists("dont-stack-if-angry", false);
        this.setIfNotExists("dont-stack-if-has-hive", false);
        this.setIfNotExists("dont-stack-if-different-hives", false);
        this.setIfNotExists("dont-stack-if-stung", false);
        this.setIfNotExists("dont-stack-if-has-flower", false);
        this.setIfNotExists("dont-stack-if-has-nectar", false);
    }

    @Override
    public void applyUnstackProperties(LivingEntity stacked, LivingEntity unstacked) {
        Bee stackedBee = (Bee) stacked;
        Bee unstackedBee = (Bee) unstacked;

        stackedBee.setAnger(unstackedBee.getAnger());

        super.applyUnstackProperties(stacked, unstacked);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.BEE;
    }

    @Override
    public Material getSpawnEggMaterial() {
        return Material.BEE_SPAWN_EGG;
    }

    @Override
    public List<String> getDefaultSpawnRequirements() {
        return Collections.emptyList();
    }

}
