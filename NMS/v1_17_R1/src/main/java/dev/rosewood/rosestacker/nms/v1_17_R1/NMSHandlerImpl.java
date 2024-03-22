package dev.rosewood.rosestacker.nms.v1_17_R1;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import dev.rosewood.rosestacker.nms.util.ReflectionUtils;
import dev.rosewood.rosestacker.nms.v1_17_R1.entity.SoloEntitySpider;
import dev.rosewood.rosestacker.nms.v1_17_R1.entity.SoloEntityStrider;
import dev.rosewood.rosestacker.nms.v1_17_R1.entity.SynchedEntityDataWrapper;
import dev.rosewood.rosestacker.nms.v1_17_R1.hologram.HologramImpl;
import dev.rosewood.rosestacker.nms.v1_17_R1.spawner.StackedSpawnerTileImpl;
import dev.rosewood.rosestacker.nms.v1_17_R1.storage.NBTEntityDataEntry;
import dev.rosewood.rosestacker.nms.v1_17_R1.storage.NBTStackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.v1_17_R1.storage.SimpleStackedEntityDataStorage;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftAbstractVillager;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.SpigotWorldConfig;
import sun.misc.Unsafe;

@SuppressWarnings("unchecked")
public class NMSHandlerImpl implements NMSHandler {

    private static EntityDataAccessor<Boolean> value_Creeper_DATA_IS_IGNITED; // DataWatcherObject that determines if a creeper is ignited, normally private

    private static Field field_GoalSelector_availableGoals; // Field to get the available pathing goals of a mob, normally private
    private static Field field_Mob_lookControl; // Field to get the look controller of a mob, normally protected
    private static Field field_Mob_moveControl; // Field to get the move controller of a mob, normally protected
    private static Field field_Mob_jumpControl; // Field to get the jump controller of a mob, normally protected
    private static Field field_LivingEntity_brain; // Field to get the brain of a living entity, normally protected

    private static Field field_ServerLevel_entityManager; // Field to get the persistent entity section manager, normally private

    private static Field field_Entity_spawnReason; // Spawn reason field (only on Paper servers, will be null for Spigot)
    private static AtomicInteger entityCounter; // Atomic integer to generate unique entity IDs, normally private

    private static Unsafe unsafe;
    private static long field_SpawnerBlockEntity_spawner_offset; // Field offset for modifying SpawnerBlockEntity's spawner field

    private static Field field_AbstractVillager_offers; // Field to get the offers of an AbstractVillager, normally private

    private static Field field_Level_spigotConfig;
    private static Field field_SpigotWorldConfig_itemDespawnRate;

    static {
        try {
            Field field_Creeper_DATA_IS_IGNITED = ReflectionUtils.getFieldByPositionAndType(net.minecraft.world.entity.monster.Creeper.class, 2, EntityDataAccessor.class);
            value_Creeper_DATA_IS_IGNITED = (EntityDataAccessor<Boolean>) field_Creeper_DATA_IS_IGNITED.get(null);
            field_GoalSelector_availableGoals = ReflectionUtils.getFieldByPositionAndType(GoalSelector.class, 0, Set.class);
            field_Mob_lookControl = ReflectionUtils.getFieldByPositionAndType(Mob.class, 0, LookControl.class);
            field_Mob_moveControl = ReflectionUtils.getFieldByPositionAndType(Mob.class, 0, MoveControl.class);
            field_Mob_jumpControl = ReflectionUtils.getFieldByPositionAndType(Mob.class, 0, JumpControl.class);
            field_LivingEntity_brain = ReflectionUtils.getFieldByPositionAndType(net.minecraft.world.entity.LivingEntity.class, 0, Brain.class);

            field_ServerLevel_entityManager = ReflectionUtils.getFieldByPositionAndType(ServerLevel.class, 0, PersistentEntitySectionManager.class);
            if (NMSAdapter.isPaper())
                field_Entity_spawnReason = ReflectionUtils.getFieldByPositionAndType(Entity.class, 0, SpawnReason.class);
            entityCounter = (AtomicInteger) ReflectionUtils.getFieldByPositionAndType(Entity.class, 0, AtomicInteger.class).get(null);

            Field field_SpawnerBlockEntity_spawner = ReflectionUtils.getFieldByPositionAndType(SpawnerBlockEntity.class, 0, BaseSpawner.class);
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
            field_SpawnerBlockEntity_spawner_offset = unsafe.objectFieldOffset(field_SpawnerBlockEntity_spawner);

            field_AbstractVillager_offers = ReflectionUtils.getFieldByPositionAndType(net.minecraft.world.entity.npc.AbstractVillager.class, 0, MerchantOffers.class);

            field_Level_spigotConfig = ReflectionUtils.getFieldByName(Level.class, "spigotConfig");
            field_SpigotWorldConfig_itemDespawnRate = ReflectionUtils.getFieldByName(SpigotWorldConfig.class, "itemDespawnRate");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public LivingEntity createNewEntityUnspawned(EntityType entityType, Location location, SpawnReason spawnReason) {
        World world = location.getWorld();
        if (world == null)
            return null;

        Class<? extends org.bukkit.entity.Entity> entityClass = entityType.getEntityClass();
        if (entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass))
            throw new IllegalArgumentException("EntityType must be of a LivingEntity");

        net.minecraft.world.entity.EntityType<? extends Entity> nmsEntityType = Registry.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(entityType.getKey()));
        Entity nmsEntity = this.createCreature(
                nmsEntityType,
                ((CraftWorld) world).getHandle(),
                null,
                null,
                null,
                new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                this.toNmsSpawnReason(spawnReason)
        );

        return nmsEntity == null ? null : (LivingEntity) nmsEntity.getBukkitEntity();
    }

    /**
     * Duplicate of {@link net.minecraft.world.entity.EntityType#create(ServerLevel, CompoundTag, Component, net.minecraft.world.entity.player.Player, BlockPos, MobSpawnType, boolean, boolean)}.
     * Contains a patch to prevent chicken jockeys from spawning and to not play the mob sound upon creation.
     */
    public <T extends Entity> T createCreature(net.minecraft.world.entity.EntityType<T> entityType, ServerLevel world, CompoundTag nbt, Component component, net.minecraft.world.entity.player.Player player, BlockPos blockPos, MobSpawnType mobSpawnType) {
        T newEntity;
        if (entityType == net.minecraft.world.entity.EntityType.SPIDER) {
            newEntity = (T) new SoloEntitySpider((net.minecraft.world.entity.EntityType<? extends Spider>) entityType, world);
        } else if (entityType == net.minecraft.world.entity.EntityType.STRIDER) {
            newEntity = (T) new SoloEntityStrider((net.minecraft.world.entity.EntityType<? extends Strider>) entityType, world);
        } else {
            newEntity = entityType.create(world);
        }

        if (newEntity == null)
            return null;

        if (field_Entity_spawnReason != null) {
            try {
                field_Entity_spawnReason.set(newEntity, this.toBukkitSpawnReason(mobSpawnType));
            } catch (IllegalAccessException ignored) { }
        }

        newEntity.moveTo(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D, Mth.wrapDegrees(world.random.nextFloat() * 360.0F), 0.0F);
        if (newEntity instanceof Mob mob) {
            mob.yHeadRot = mob.getYRot();
            mob.yBodyRot = mob.getYRot();

            SpawnGroupData groupDataEntity = null;
            if (entityType == net.minecraft.world.entity.EntityType.DROWNED
                    || entityType == net.minecraft.world.entity.EntityType.HUSK
                    || entityType == net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER
                    || entityType == net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN
                    || entityType == net.minecraft.world.entity.EntityType.ZOMBIE) {
                // Don't allow chicken jockeys to spawn
                groupDataEntity = new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(world.getRandom()), false);
            }

            mob.finalizeSpawn(world, world.getCurrentDifficultyAt(mob.blockPosition()), mobSpawnType, groupDataEntity, nbt);
        }

        if (component != null && newEntity instanceof Mob)
            newEntity.setCustomName(component);

        net.minecraft.world.entity.EntityType.updateCustomEntityTag(world, player, newEntity, nbt);

        return newEntity;
    }

    @Override
    public void spawnExistingEntity(LivingEntity entity, SpawnReason spawnReason, boolean bypassSpawnEvent) {
        Location location = entity.getLocation();
        World world = location.getWorld();
        if (world == null)
            throw new IllegalArgumentException("Entity is not in a loaded world");

        if (bypassSpawnEvent) {
            ((CraftWorld) world).getHandle().entityManager.addNewEntity(((CraftEntity) entity).getHandle());
        } else {
            ((CraftWorld) world).addEntityToWorld(((CraftEntity) entity).getHandle(), spawnReason);
        }
    }

    @Override
    public void updateEntityNameTagForPlayer(Player player, org.bukkit.entity.Entity entity, String customName, boolean customNameVisible) {
        try {
            List<SynchedEntityData.DataItem<?>> dataItems = new ArrayList<>();
            Optional<Component> nameComponent = Optional.ofNullable(CraftChatMessage.fromStringOrNull(customName));
            dataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.OPTIONAL_COMPONENT.createAccessor(2), nameComponent));
            dataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.BOOLEAN.createAccessor(3), customNameVisible));

            ClientboundSetEntityDataPacket entityDataPacket = new ClientboundSetEntityDataPacket(entity.getEntityId(), new SynchedEntityDataWrapper(dataItems), false);
            ((CraftPlayer) player).getHandle().connection.send(entityDataPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateEntityNameTagVisibilityForPlayer(Player player, org.bukkit.entity.Entity entity, boolean customNameVisible) {
        try {
            List<SynchedEntityData.DataItem<?>> dataItems = Lists.newArrayList(new SynchedEntityData.DataItem<>(EntityDataSerializers.BOOLEAN.createAccessor(3), customNameVisible));
            ClientboundSetEntityDataPacket entityDataPacket = new ClientboundSetEntityDataPacket(entity.getEntityId(), new SynchedEntityDataWrapper(dataItems), false);
            ((CraftPlayer) player).getHandle().connection.send(entityDataPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unigniteCreeper(Creeper creeper) {
        net.minecraft.world.entity.monster.Creeper nmsCreeper = ((CraftCreeper) creeper).getHandle();

        nmsCreeper.getEntityData().set(value_Creeper_DATA_IS_IGNITED, false);
        if (!Bukkit.getBukkitVersion().contains("1.17-"))
            nmsCreeper.swell = nmsCreeper.maxSwell;
    }

    @Override
    public void removeEntityGoals(LivingEntity livingEntity) {
        net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) livingEntity).getHandle();
        if (!(nmsEntity instanceof Mob))
            return;

        try {
            Mob mob = (Mob) nmsEntity;

            // Remove all goal AI other than floating in water
            Set<WrappedGoal> goals = (Set<WrappedGoal>) field_GoalSelector_availableGoals.get(mob.goalSelector);
            Iterator<WrappedGoal> goalsIterator = goals.iterator();
            while (goalsIterator.hasNext()) {
                WrappedGoal goal = goalsIterator.next();
                if (goal.getGoal() instanceof FloatGoal)
                    continue;

                goalsIterator.remove();
            }

            // Remove all targetting AI
            ((Set<WrappedGoal>) field_GoalSelector_availableGoals.get(mob.targetSelector)).clear();

            // Forget any existing targets
            mob.setTarget(null);

            // Remove controllers
            field_Mob_lookControl.set(mob, new LookControl(mob) {
                public void tick() { }
            });
            field_Mob_moveControl.set(mob, new MoveControl(mob) {
                public void tick() { }
            });
            if (!(mob instanceof Rabbit)) {
                field_Mob_jumpControl.set(mob, new JumpControl(mob) {
                    public void tick() { }
                });
            }
            field_LivingEntity_brain.set(mob, new Brain(List.of(), List.of(), ImmutableList.of(), () -> Brain.codec(List.of(), List.of())) {
                public Optional<?> getMemory(MemoryModuleType var0) { return Optional.empty(); }
            });
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ItemStack setItemStackNBT(ItemStack itemStack, String key, String value) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag tagCompound = nmsItem.getOrCreateTag();
        tagCompound.putString(key, value);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public ItemStack setItemStackNBT(ItemStack itemStack, String key, int value) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag tagCompound = nmsItem.getOrCreateTag();
        tagCompound.putInt(key, value);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public String getItemStackNBTString(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag tagCompound = nmsItem.getOrCreateTag();
        return tagCompound.getString(key);
    }

    @Override
    public int getItemStackNBTInt(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag tagCompound = nmsItem.getOrCreateTag();
        return tagCompound.getInt(key);
    }

    @Override
    public String getItemStackNBTStringFromCompound(ItemStack itemStack, String compoundKey, String valueKey) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag tagCompound = nmsItem.getOrCreateTag();
        CompoundTag targetCompound = tagCompound.getCompound(compoundKey);
        if (targetCompound == null)
            return "";
        return targetCompound.getString(valueKey);
    }

    @Override
    public void setLastHurtBy(LivingEntity livingEntity, Player player) {
        if (player != null)
            ((CraftLivingEntity) livingEntity).getHandle().lastHurtByPlayer = ((CraftPlayer) player).getHandle();
    }

    @Override
    public boolean hasLineOfSight(LivingEntity entity1, Location location) {
        net.minecraft.world.entity.LivingEntity nmsEntity1 = ((CraftLivingEntity) entity1).getHandle();
        Vec3 vec3d = new Vec3(nmsEntity1.getX(), nmsEntity1.getEyeY(), nmsEntity1.getZ());
        Vec3 target = new Vec3(location.getX(), location.getY(), location.getZ());
        return nmsEntity1.level.clip(new ClipContext(vec3d, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, nmsEntity1)).getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean isActiveRaider(LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle() instanceof Raider raider && raider.getCurrentRaid() != null;
    }

    @Override
    public EntityDataEntry createEntityDataEntry(LivingEntity livingEntity) {
        return new NBTEntityDataEntry(livingEntity);
    }

    public StackedEntityDataStorage createEntityDataStorage(LivingEntity livingEntity, StackedEntityDataStorageType storageType) {
        return switch (storageType) {
            case NBT -> new NBTStackedEntityDataStorage(livingEntity);
            case SIMPLE -> new SimpleStackedEntityDataStorage(livingEntity);
        };
    }

    public StackedEntityDataStorage deserializeEntityDataStorage(LivingEntity livingEntity, byte[] data, StackedEntityDataStorageType storageType) {
        return switch (storageType) {
            case NBT -> new NBTStackedEntityDataStorage(livingEntity, data);
            case SIMPLE -> new SimpleStackedEntityDataStorage(livingEntity, data);
        };
    }

    @Override
    public StackedSpawnerTile injectStackedSpawnerTile(Object stackedSpawnerObj) {
        StackedSpawner stackedSpawner = (StackedSpawner) stackedSpawnerObj;
        Block block = stackedSpawner.getBlock();
        ServerLevel level = ((CraftWorld) block.getWorld()).getHandle();
        BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity))
            return null;

        StackedSpawnerTile stackedSpawnerTile = new StackedSpawnerTileImpl(spawnerBlockEntity.getSpawner(), spawnerBlockEntity, stackedSpawner);
        unsafe.putObject(spawnerBlockEntity, field_SpawnerBlockEntity_spawner_offset, stackedSpawnerTile);
        return stackedSpawnerTile;
    }

    @Override
    public Hologram createHologram(Location location, List<String> text) {
        return new HologramImpl(text, location, entityCounter::incrementAndGet);
    }

    @Override
    public void setCustomNameUncapped(org.bukkit.entity.Entity entity, String customName) {
        ((CraftEntity) entity).getHandle().setCustomName(CraftChatMessage.fromStringOrNull(customName));
    }

    @Override
    public int getItemDespawnRate(Item item) {
        try {
            return field_SpigotWorldConfig_itemDespawnRate.getInt(field_Level_spigotConfig.get(((CraftWorld) item.getWorld()).getHandle()));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            throw new IllegalStateException("Unable to get item despawn rate");
        }
    }

    private SpawnReason toBukkitSpawnReason(MobSpawnType mobSpawnType) {
        return switch (mobSpawnType) {
            case SPAWN_EGG -> SpawnReason.SPAWNER_EGG;
            case SPAWNER -> SpawnReason.SPAWNER;
            default -> SpawnReason.CUSTOM;
        };
    }

    private MobSpawnType toNmsSpawnReason(SpawnReason spawnReason) {
        return switch (spawnReason) {
            case SPAWNER_EGG -> MobSpawnType.SPAWN_EGG;
            case SPAWNER -> MobSpawnType.SPAWNER;
            default -> MobSpawnType.COMMAND;
        };
    }

    public void saveEntityToTag(LivingEntity livingEntity, CompoundTag compoundTag) {
        // Async villager "fix", if the trades aren't loaded yet force them to save as empty, they will get loaded later
        if (livingEntity instanceof AbstractVillager) {
            try {
                net.minecraft.world.entity.npc.AbstractVillager villager = ((CraftAbstractVillager) livingEntity).getHandle();

                // Set the trades to empty if they are null to prevent trades from generating during the saveWithoutId call
                boolean bypassTrades = field_AbstractVillager_offers.get(villager) == null;
                if (bypassTrades)
                    field_AbstractVillager_offers.set(villager, new MerchantOffers());

                ((CraftLivingEntity) livingEntity).getHandle().saveWithoutId(compoundTag);

                // Restore the offers back to null and make sure nothing is written to the NBT
                if (bypassTrades) {
                    field_AbstractVillager_offers.set(villager, null);
                    compoundTag.remove("Offers");
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else {
            ((CraftLivingEntity) livingEntity).getHandle().saveWithoutId(compoundTag);
        }
    }

    public void registerEntity(ServerLevel world, Entity entity) {
        try {
            PersistentEntitySectionManager<Entity> entityManager = (PersistentEntitySectionManager<Entity>) field_ServerLevel_entityManager.get(world);
            entityManager.addNewEntity(entity);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
