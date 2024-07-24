package dev.rosewood.rosestacker.stack.settings.conditions.entity;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.stack.EntityStackComparisonResult;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.settings.EntityStackSettings;
import dev.rosewood.rosestacker.utils.PersistentDataUtils;
import dev.rosewood.rosestacker.utils.VersionUtils;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Allay;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Cat;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.material.Colorable;

public final class StackConditions {

    private static final Multimap<Class<?>, StackCondition<?>> CLASS_STACK_EVALUATION_MAP;
    private static final NMSHandler NMS_HANDLER = NMSAdapter.getHandler();

    static {
        CLASS_STACK_EVALUATION_MAP = MultimapBuilder.linkedHashKeys().arrayListValues().build();

        // Register base Entity conditions
        register(Entity.class, (stackSettings, stack1, stack2, entity1, entity2, comparingForUnstack, ignorePositions) -> {
            int offset = comparingForUnstack ? -1 : 0;
            if (entity1 == entity2) {
                if (stack1.getStackSize() + 1 + offset > stackSettings.getMaxStackSize())
                    return EntityStackComparisonResult.STACK_SIZE_TOO_LARGE;
            } else {
                if (entity1.getType() != entity2.getType())
                    return EntityStackComparisonResult.DIFFERENT_ENTITY_TYPES;

                if (stack1.getStackSize() + stack2.getStackSize() + offset > stackSettings.getMaxStackSize())
                    return EntityStackComparisonResult.STACK_SIZE_TOO_LARGE;
            }

            if (!stackSettings.isStackingEnabled())
                return EntityStackComparisonResult.STACKING_NOT_ENABLED;

            if (PersistentDataUtils.isUnstackable(entity1) || PersistentDataUtils.isUnstackable(entity2))
                return EntityStackComparisonResult.MARKED_UNSTACKABLE;

            if (SettingKey.ENTITY_DONT_STACK_CUSTOM_NAMED.get() && (entity1.getCustomName() != null || entity2.getCustomName() != null)
                    && (entity1.getType() != VersionUtils.SNOW_GOLEM || !stackSettings.getSettingValue(EntityStackSettings.SNOW_GOLEM_FORCE_CUSTOM_NAMED_STACKING).getBoolean())) // Force named snow golems to always stack together for infinite snowball lag-prevention reasons
                return EntityStackComparisonResult.CUSTOM_NAMED;

            if (!comparingForUnstack && !ignorePositions && !stackSettings.getEntityTypeData().swimmingMob() && !stackSettings.getEntityTypeData().flyingMob()) {
                if (SettingKey.ENTITY_ONLY_STACK_ON_GROUND.get() && (!entity1.isOnGround() || !entity2.isOnGround()))
                    return EntityStackComparisonResult.NOT_ON_GROUND;

                if (SettingKey.ENTITY_DONT_STACK_IF_IN_WATER.get() &&
                        (entity1.getLocation().getBlock().getType() == Material.WATER || entity2.getLocation().getBlock().getType() == Material.WATER))
                    return EntityStackComparisonResult.IN_WATER;
            }

            if (!comparingForUnstack && stackSettings.shouldOnlyStackFromSpawners() &&
                    (!PersistentDataUtils.isSpawnedFromSpawner(entity1) || !PersistentDataUtils.isSpawnedFromSpawner(entity2)))
                return EntityStackComparisonResult.NOT_SPAWNED_FROM_SPAWNER;

            // Don't stack if being ridden or is riding something
            if (!comparingForUnstack && (!entity1.getPassengers().isEmpty() || !entity2.getPassengers().isEmpty() || entity1.isInsideVehicle() || entity2.isInsideVehicle()))
                return EntityStackComparisonResult.PART_OF_VEHICLE; // If comparing for unstack and is being ridden or is riding something, don't want to unstack it

            if (SettingKey.ENTITY_DONT_STACK_IF_INVULNERABLE.get() && (entity1.isInvulnerable() || entity2.isInvulnerable()))
                return EntityStackComparisonResult.INVULNERABLE;

            return EntityStackComparisonResult.CAN_STACK;
        });

        // Register base LivingEntity conditions
        register(LivingEntity.class, (stackSettings, stack1, stack2, entity1, entity2, comparingForUnstack, ignorePositions) -> {
            if (!comparingForUnstack && SettingKey.ENTITY_DONT_STACK_IF_LEASHED.get() && (entity1.isLeashed() || entity2.isLeashed()))
                return EntityStackComparisonResult.LEASHED;

            if (SettingKey.ENTITY_DONT_STACK_IF_HAS_EQUIPMENT.get()) {
                EntityEquipment equipment1 = entity1.getEquipment();
                EntityEquipment equipment2 = entity2.getEquipment();

                if (equipment1 != null) {
                    for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                        ItemStack item = equipment1.getItem(equipmentSlot);
                        if (item.getType() != Material.AIR && !stackSettings.getEntityTypeData().isStandardEquipment(item))
                            return EntityStackComparisonResult.HAS_EQUIPMENT;
                    }
                }

                if (equipment2 != null) {
                    for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                        ItemStack item = equipment2.getItem(equipmentSlot);
                        if (item.getType() != Material.AIR && !stackSettings.getEntityTypeData().isStandardEquipment(item))
                            return EntityStackComparisonResult.HAS_EQUIPMENT;
                    }
                }
            }

            if (SettingKey.ENTITY_DONT_STACK_IF_ACTIVE_RAIDER.get() && (NMS_HANDLER.isActiveRaider(entity1) || NMS_HANDLER.isActiveRaider(entity2)))
                return EntityStackComparisonResult.PART_OF_ACTIVE_RAID;

            return EntityStackComparisonResult.CAN_STACK;
        });

        // Register conditions for specific interfaces
        registerConfig(Colorable.class, "different-color", false, EntityStackComparisonResult.DIFFERENT_COLORS, (entity1, entity2) -> entity1.getColor() != entity2.getColor());
        registerConfig(Sittable.class, "sitting", false, EntityStackComparisonResult.SITTING, (entity1, entity2) -> entity1.isSitting() || entity2.isSitting());
        registerConfig(Tameable.class, "tamed", false, EntityStackComparisonResult.TAMED, (entity1, entity2) -> entity1.isTamed() || entity2.isTamed());
        registerConfig(Tameable.class, "different-owners", false, EntityStackComparisonResult.DIFFERENT_OWNERS, (entity1, entity2) -> {
            AnimalTamer tamer1 = entity1.getOwner();
            AnimalTamer tamer2 = entity2.getOwner();
            return tamer1 != null && tamer2 != null && !tamer1.getUniqueId().equals(tamer2.getUniqueId());
        });
        registerConfig(Animals.class, "breeding", false, EntityStackComparisonResult.BREEDING, (entity1, entity2) -> {
            if (entity1.getType() == EntityType.TURTLE) {
                Turtle turtle1 = (Turtle) entity1;
                Turtle turtle2 = (Turtle) entity2;
                if (turtle1.hasEgg() || turtle1.isLayingEgg() || turtle2.hasEgg() || turtle2.isLayingEgg())
                    return true;
            }
            return entity1.isLoveMode() || entity2.isLoveMode() || (!entity1.canBreed() && entity1.isAdult()) || (!entity2.canBreed() && entity2.isAdult());
        });
        registerConfig(Ageable.class, "different-age", true, EntityStackComparisonResult.DIFFERENT_AGES, (entity1, entity2) -> entity1.isAdult() != entity2.isAdult());
        registerConfig(Ageable.class, "baby", false, EntityStackComparisonResult.BABY, (entity1, entity2) -> !entity1.isAdult() || !entity2.isAdult());
        registerConfig(AbstractHorse.class, "saddled", false, EntityStackComparisonResult.SADDLED, (entity1, entity2) -> entity1.getInventory().getSaddle() != null || entity2.getInventory().getSaddle() != null);
        registerConfig(ChestedHorse.class, "chested", false, EntityStackComparisonResult.HAS_CHEST, (entity1, entity2) -> entity1.isCarryingChest() || entity2.isCarryingChest());
        registerConfig(Raider.class, "patrol-leader", false, EntityStackComparisonResult.PATROL_LEADER, (entity1, entity2) -> entity1.isPatrolLeader() || entity2.isPatrolLeader());
        registerConfig(Merchant.class, "trading", false, EntityStackComparisonResult.TRADING, (entity1, entity2) -> entity1.isTrading() || entity2.isTrading());

        // Register conditions for specific entities
        int versionNumber = NMSUtil.getVersionNumber();
        int minorVersionNumber = NMSUtil.getMinorVersionNumber();
        if (versionNumber > 20 || (versionNumber == 20 && minorVersionNumber >= 5)) {
            // Armadillo, Bogged, Breeze
            // TODO: None of these mobs have API as of 28/April/2024
        }

        if (versionNumber >= 19) {
            // Allay, Frog, Goat (extras), Tadpole, Warden
            registerConfig(Allay.class, "holding-items", false, EntityStackComparisonResult.HOLDING_ITEMS, (entity1, entity2) -> !entity1.getInventory().isEmpty() || !entity2.getInventory().isEmpty());
            registerConfig(Frog.class, "different-type", false, EntityStackComparisonResult.DIFFERENT_TYPES, (entity1, entity2) -> entity1.getVariant() != entity2.getVariant());
            registerConfig(Goat.class, "different-horns", false, EntityStackComparisonResult.DIFFERENT_HORNS, (entity1, entity2) -> entity1.hasLeftHorn() != entity2.hasLeftHorn() || entity1.hasRightHorn() != entity2.hasRightHorn());

            // 1.19.3+
            if (NMSAdapter.getHandler().supportsEmptySpawners()) {
                registerConfig(Camel.class, "dashing", false, EntityStackComparisonResult.DASHING, (entity1, entity2) -> entity1.isDashing() || entity2.isDashing());
            }
        }

        if (versionNumber >= 17) {
            // Axolotl, Glow Squid, Goat
            registerConfig(Axolotl.class, "different-color", false, EntityStackComparisonResult.DIFFERENT_COLORS, (entity1, entity2) -> entity1.getVariant() != entity2.getVariant());
            registerConfig(Axolotl.class, "playing-dead", false, EntityStackComparisonResult.PLAYING_DEAD, (entity1, entity2) -> entity1.isPlayingDead() || entity2.isPlayingDead());
            registerConfig(GlowSquid.class, "dark", false, EntityStackComparisonResult.BRAVO_SIX_GOING_DARK, (entity1, entity2) -> entity1.getDarkTicksRemaining() > 0 || entity2.getDarkTicksRemaining() > 0);
            registerConfig(Goat.class, "screaming", false, EntityStackComparisonResult.SCREAMING, (entity1, entity2) -> entity1.isScreaming() || entity2.isScreaming());
        }

        // Everything else
        registerConfig(Bat.class, "sleeping", false, EntityStackComparisonResult.SLEEPING, (entity1, entity2) -> !entity1.isAwake() || !entity2.isAwake());
        registerConfig(Bee.class, "angry", false, EntityStackComparisonResult.ANGRY, (entity1, entity2) -> entity1.getAnger() > 0 || entity2.getAnger() > 0);
        registerConfig(Bee.class, "has-hive", false, EntityStackComparisonResult.HAS_HIVE, (entity1, entity2) -> entity1.getHive() != null || entity2.getHive() != null);
        registerConfig(Bee.class, "different-hives", false, EntityStackComparisonResult.DIFFERENT_HIVES, (entity1, entity2) -> entity1.getHive() != null && entity2.getHive() != null && !entity1.getHive().equals(entity2.getHive()));
        registerConfig(Bee.class, "stung", false, EntityStackComparisonResult.HAS_STUNG, (entity1, entity2) -> entity1.hasStung() || entity2.hasStung());
        registerConfig(Bee.class, "has-flower", false, EntityStackComparisonResult.HAS_FLOWER, (entity1, entity2) -> entity1.getFlower() != null || entity2.getFlower() != null);
        registerConfig(Bee.class, "has-nectar", false, EntityStackComparisonResult.HAS_NECTAR, (entity1, entity2) -> entity1.hasNectar() || entity2.hasNectar());
        registerConfig(Cat.class, "different-type", false, EntityStackComparisonResult.DIFFERENT_TYPES, (entity1, entity2) -> entity1.getCatType() != entity2.getCatType());
        registerConfig(Cat.class, "different-collar-color", false, EntityStackComparisonResult.DIFFERENT_COLLAR_COLORS, (entity1, entity2) -> entity1.getCollarColor() != entity2.getCollarColor());
        registerConfig(Creeper.class, "charged", false, EntityStackComparisonResult.CHARGED, (entity1, entity2) -> entity1.isPowered() || entity2.isPowered());
        registerConfig(Enderman.class, "holding-block", false, EntityStackComparisonResult.HOLDING_BLOCK, (entity1, entity2) -> entity1.getCarriedBlock() != null || entity2.getCarriedBlock() != null);
        registerConfig(Fox.class, "different-type", false, EntityStackComparisonResult.DIFFERENT_TYPES, (entity1, entity2) -> entity1.getFoxType() != entity2.getFoxType());
        registerConfig(Hoglin.class, "unhuntable", false, EntityStackComparisonResult.UNHUNTABLE, (entity1, entity2) -> !entity1.isAbleToBeHunted() || entity2.isAbleToBeHunted());
        registerConfig(Horse.class, "armored", false, EntityStackComparisonResult.HAS_ARMOR, (entity1, entity2) -> entity1.getInventory().getArmor() != null || entity2.getInventory().getArmor() != null);
        registerConfig(Horse.class, "different-style", false, EntityStackComparisonResult.DIFFERENT_STYLES, (entity1, entity2) -> entity1.getStyle() != entity2.getStyle());
        registerConfig(Horse.class, "different-color", false, EntityStackComparisonResult.DIFFERENT_COLORS, (entity1, entity2) -> entity1.getColor() != entity2.getColor());
        registerConfig(IronGolem.class, "player-created", false, EntityStackComparisonResult.SPAWNED_BY_PLAYER, (entity1, entity2) -> entity1.isPlayerCreated() || entity2.isPlayerCreated());
        registerConfig(Llama.class, "different-decor", false, EntityStackComparisonResult.DIFFERENT_DECORS, (entity1, entity2) -> entity1.getInventory().getDecor() != entity2.getInventory().getDecor());
        registerConfig(Llama.class, "different-color", false, EntityStackComparisonResult.DIFFERENT_COLORS, (entity1, entity2) -> entity1.getColor() != entity2.getColor());
        registerConfig(MushroomCow.class, "different-type", false, EntityStackComparisonResult.DIFFERENT_TYPES, (entity1, entity2) -> entity1.getVariant() != entity2.getVariant());
        registerConfig(Panda.class, "different-main-gene", false, EntityStackComparisonResult.DIFFERENT_MAIN_GENES, (entity1, entity2) -> entity1.getMainGene() != entity2.getMainGene());
        registerConfig(Panda.class, "different-recessive-gene", false, EntityStackComparisonResult.DIFFERENT_RECESSIVE_GENES, (entity1, entity2) -> entity1.getHiddenGene() != entity2.getHiddenGene());
        registerConfig(Parrot.class, "different-type", false, EntityStackComparisonResult.DIFFERENT_TYPES, (entity1, entity2) -> entity1.getVariant() != entity2.getVariant());
        registerConfig(Phantom.class, "different-size", true, EntityStackComparisonResult.DIFFERENT_SIZES, (entity1, entity2) -> entity1.getSize() != entity2.getSize());
        registerConfig(PiglinAbstract.class, "converting", false, EntityStackComparisonResult.CONVERTING, (entity1, entity2) -> entity1.isConverting() || entity2.isConverting());
        registerConfig(PiglinAbstract.class, "immune-to-zombification", false, EntityStackComparisonResult.IMMUNE_TO_ZOMBIFICATION, (entity1, entity2) -> entity1.isImmuneToZombification() != entity2.isImmuneToZombification());
        registerConfig(Piglin.class, "unable-to-hunt", false, EntityStackComparisonResult.UNABLE_TO_HUNT, (entity1, entity2) -> !entity1.isAbleToHunt() || !entity2.isAbleToHunt());
        registerConfig(PigZombie.class, "angry", false, EntityStackComparisonResult.ANGRY, (entity1, entity2) -> entity1.isAngry() || entity2.isAngry());
        registerConfig(Pig.class, "saddled", false, EntityStackComparisonResult.SADDLED, (entity1, entity2) -> entity1.hasSaddle() || entity2.hasSaddle());
        registerConfig(PufferFish.class, "different-inflation", false, EntityStackComparisonResult.DIFFERENT_INFLATIONS, (entity1, entity2) -> entity1.getPuffState() != entity2.getPuffState());
        registerConfig(Rabbit.class, "different-type", false, EntityStackComparisonResult.DIFFERENT_TYPES, (entity1, entity2) -> entity1.getRabbitType() != entity2.getRabbitType());
        registerConfig(Sheep.class, "sheared", false, EntityStackComparisonResult.SHEARED, (entity1, entity2) -> entity1.isSheared() || entity2.isSheared());
        registerConfig(Sheep.class, "different-shear-state", false, EntityStackComparisonResult.SHEARED_STATE_DIFFERENT, (entity1, entity2) -> entity1.isSheared() != entity2.isSheared());
        registerConfig(Slime.class, "different-size", true, EntityStackComparisonResult.DIFFERENT_SIZES, (entity1, entity2) -> entity1.getSize() != entity2.getSize());
        registerConfig(Snowman.class, "no-pumpkin", false, EntityStackComparisonResult.NO_PUMPKIN, (entity1, entity2) -> entity1.isDerp() || entity2.isDerp());
        registerConfig(Strider.class, "shivering", false, EntityStackComparisonResult.SHIVERING, (entity1, entity2) -> entity1.isShivering() || entity2.isShivering());
        registerConfig(Strider.class, "saddled", false, EntityStackComparisonResult.SADDLED, (entity1, entity2) -> entity1.hasSaddle() || entity2.hasSaddle());
        registerConfig(TropicalFish.class, "different-body-color", false, EntityStackComparisonResult.DIFFERENT_BODY_COLORS, (entity1, entity2) -> entity1.getBodyColor() != entity2.getBodyColor());
        registerConfig(TropicalFish.class, "different-pattern", false, EntityStackComparisonResult.DIFFERENT_PATTERNS, (entity1, entity2) -> entity1.getPattern() != entity2.getPattern());
        registerConfig(TropicalFish.class, "different-pattern-color", false, EntityStackComparisonResult.DIFFERENT_PATTERN_COLORS, (entity1, entity2) -> entity1.getPatternColor() != entity2.getPatternColor());
        registerConfig(Vex.class, "charging", false, EntityStackComparisonResult.CHARGING, (entity1, entity2) -> entity1.isCharging() || entity2.isCharging());
        registerConfig(Villager.class, "professioned", false, EntityStackComparisonResult.PROFESSIONED, (entity1, entity2) -> {
            List<String> professionValues = List.of("NONE", "NITWIT");
            return !professionValues.contains(entity1.getProfession().name()) || !professionValues.contains(entity2.getProfession().name());
        });
        registerConfig(Villager.class, "different-profession", false, EntityStackComparisonResult.DIFFERENT_PROFESSIONS, (entity1, entity2) -> entity1.getProfession() != entity2.getProfession());
        registerConfig(Villager.class, "different-type", false, EntityStackComparisonResult.DIFFERENT_TYPES, (entity1, entity2) -> entity1.getVillagerType() != entity2.getVillagerType());
        registerConfig(Villager.class, "different-level", false, EntityStackComparisonResult.DIFFERENT_LEVELS, (entity1, entity2) -> entity1.getVillagerLevel() != entity2.getVillagerLevel());
        registerConfig(Wolf.class, "angry", false, EntityStackComparisonResult.ANGRY, (entity1, entity2) -> entity1.isAngry() || entity2.isAngry());
        registerConfig(Wolf.class, "different-collar-color", false, EntityStackComparisonResult.DIFFERENT_COLLAR_COLORS, (entity1, entity2) -> entity1.getCollarColor() != entity2.getCollarColor());
        registerConfig(Zombie.class, "converting", false, EntityStackComparisonResult.CONVERTING, (entity1, entity2) -> entity1.isConverting() || entity2.isConverting());
        registerConfig(ZombieVillager.class, "different-profession", false, EntityStackComparisonResult.DIFFERENT_PROFESSIONS, (entity1, entity2) -> entity1.getVillagerProfession() != entity2.getVillagerProfession());
        registerConfig(ZombieVillager.class, "converting", false, EntityStackComparisonResult.CONVERTING, (entity1, entity2) -> entity1.isConverting() || entity2.isConverting());
    }

    private StackConditions() {

    }

    public static List<StackCondition<?>> getEligibleConditions(Class<? extends Entity> entityClass) {
        return CLASS_STACK_EVALUATION_MAP.keySet().stream()
                .filter(x -> x.isAssignableFrom(entityClass))
                .flatMap(x -> CLASS_STACK_EVALUATION_MAP.get(x).stream())
                .toList();
    }

    public static <T> void registerConfig(Class<T> assignableClass, String key, boolean defaultEnabled, EntityStackComparisonResult failureReason, StackValidationPredicate<T> validationPredicate) {
        registerInternal(assignableClass, new ConfigProperties("dont-stack-if-" + key, defaultEnabled), (stackSettings, stack1, stack2, entity1, entity2, comparingForUnstack, ignorePositions)
                -> validationPredicate.test(entity1, entity2) ? failureReason : EntityStackComparisonResult.CAN_STACK);
    }

    public static <T> void register(Class<T> assignableClass, StackValidationFunction<T> validationFunction) {
        registerInternal(assignableClass, null, validationFunction);
    }

    private static <T, P extends StackValidationFunction<T>> void registerInternal(Class<T> assignableClass, ConfigProperties configProperties, P validationFunction) {
        CLASS_STACK_EVALUATION_MAP.put(assignableClass, new StackCondition<>(assignableClass, configProperties, validationFunction));
    }

    public record ConfigProperties(String key, boolean defaultValue) { }

    public record StackCondition<T>(Class<T> clazz, ConfigProperties configProperties, StackValidationFunction<T> function) { }

    @FunctionalInterface
    public interface StackValidationFunction<T> {
        EntityStackComparisonResult apply(EntityStackSettings stackSettings, StackedEntity stack1,
                                          StackedEntity stack2, T entity1, T entity2, boolean comparingForUnstack,
                                          boolean ignorePositions);
    }

    @FunctionalInterface
    public interface StackValidationPredicate<T> {
        boolean test(T entity1, T entity2);
    }

}
