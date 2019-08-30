package dev.esophose.rosestacker.entitydata;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.function.Consumer;

public abstract class GenericEntityData<T extends LivingEntity> implements EntityData {

    private Collection<PotionEffect> activePotionEffects;
    private boolean canPickupItems;
    private boolean collidable;
    private String customName;
    private boolean customNameVisible;
    private int fireTicks;
    private boolean glowing;
    private double health;
    private boolean invulnerable;
    private int maximumAir;
    private int maximumNoDamageTicks;
    private int noDamageTicks;
    private boolean op;
    private int remainingAir;
    private boolean removeWhenFarAway;
    private boolean silent;
    private int ticksLived;

    public GenericEntityData(T entity) {
        this.activePotionEffects = entity.getActivePotionEffects();
        this.canPickupItems = entity.getCanPickupItems();
        this.collidable = entity.isCollidable();
        this.customName = entity.getCustomName();
        this.customNameVisible = entity.isCustomNameVisible();
        this.fireTicks = entity.getFireTicks();
        this.glowing = entity.isGlowing();
        this.health = entity.getHealth();
        this.invulnerable = entity.isInvulnerable();
        this.maximumAir = entity.getMaximumAir();
        this.maximumNoDamageTicks = entity.getMaximumNoDamageTicks();
        this.noDamageTicks = entity.getNoDamageTicks();
        this.op = entity.isOp();
        this.remainingAir = entity.getRemainingAir();
        this.removeWhenFarAway = entity.getRemoveWhenFarAway();
        this.silent = entity.isSilent();
        this.ticksLived = entity.getTicksLived();
    }

    public T spawnAt(Location location) {
        return this.spawnAtInternal(location, (entity) -> {
            this.activePotionEffects.forEach(entity::addPotionEffect);
            entity.setCanPickupItems(this.canPickupItems);
            entity.setCollidable(this.collidable);
            entity.setCustomName(this.customName);
            entity.setCustomNameVisible(this.customNameVisible);
            entity.setFireTicks(this.fireTicks);
            entity.setGlowing(this.glowing);
            entity.setHealth(this.health);
            entity.setInvulnerable(this.invulnerable);
            entity.setMaximumAir(this.maximumAir);
            entity.setMaximumNoDamageTicks(this.maximumNoDamageTicks);
            entity.setNoDamageTicks(this.noDamageTicks);
            entity.setOp(this.op);
            entity.setRemainingAir(this.remainingAir);
            entity.setRemoveWhenFarAway(this.removeWhenFarAway);
            entity.setSilent(this.silent);
            entity.setTicksLived(this.ticksLived);
        });
    }

    protected abstract T spawnAtInternal(Location location, Consumer<T> action);

}
