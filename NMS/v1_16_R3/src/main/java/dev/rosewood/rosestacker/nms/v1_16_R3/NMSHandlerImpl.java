package dev.rosewood.rosestacker.nms.v1_16_R3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.nms.spawner.StackedSpawnerTile;
import dev.rosewood.rosestacker.nms.storage.EntityDataEntry;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorageType;
import dev.rosewood.rosestacker.nms.util.ReflectionUtils;
import dev.rosewood.rosestacker.nms.v1_16_R3.entity.DataWatcherWrapper;
import dev.rosewood.rosestacker.nms.v1_16_R3.entity.SoloEntitySpider;
import dev.rosewood.rosestacker.nms.v1_16_R3.entity.SoloEntityStrider;
import dev.rosewood.rosestacker.nms.v1_16_R3.hologram.HologramImpl;
import dev.rosewood.rosestacker.nms.v1_16_R3.spawner.StackedSpawnerTileImpl;
import dev.rosewood.rosestacker.nms.v1_16_R3.storage.NBTEntityDataEntry;
import dev.rosewood.rosestacker.nms.v1_16_R3.storage.NBTStackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.v1_16_R3.storage.SimpleStackedEntityDataStorage;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.v1_16_R3.BehaviorController;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkStatus;
import net.minecraft.server.v1_16_R3.ControllerJump;
import net.minecraft.server.v1_16_R3.ControllerLook;
import net.minecraft.server.v1_16_R3.ControllerMove;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityCreeper;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityRabbit;
import net.minecraft.server.v1_16_R3.EntityRaider;
import net.minecraft.server.v1_16_R3.EntitySpider;
import net.minecraft.server.v1_16_R3.EntityStrider;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EntityVillagerAbstract;
import net.minecraft.server.v1_16_R3.EntityZombie;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.GroupDataEntity;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IChunkAccess;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.MerchantRecipeList;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.MovingObjectPosition;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_16_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_16_R3.PathfinderGoalWrapped;
import net.minecraft.server.v1_16_R3.RayTrace;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftAbstractVillager;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
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

    private static Method method_WorldServer_registerEntity; // Method to register an entity into a world

    private static DataWatcherObject<Boolean> value_EntityCreeper_d; // DataWatcherObject that determines if a creeper is ignited, normally private
    private static Field field_EntityCreeper_fuseTicks; // Field to set the remaining fuse ticks of a creeper, normally private

    private static Field field_PathfinderGoalSelector_d; // Field to get a PathfinderGoalSelector of an insentient entity, normally private
    private static Field field_EntityInsentient_lookController; // Field to get the look controller of an insentient entity, normally protected
    private static Field field_EntityInsentient_moveController; // Field to get the move controller of an insentient entity, normally protected
    private static Field field_EntityInsentient_jumpController; // Field to get the jump controller of an insentient entity, normally protected
    private static Field field_EntityLiving_behaviorController; // Field to get the behavior controller of a living entity, normally protected

    private static Field field_Entity_spawnReason; // Spawn reason field (only on Paper servers, will be null for Spigot)
    private static AtomicInteger entityCounter; // Atomic integer to generate unique entity IDs, normally private

    private static Unsafe unsafe;
    private static long field_SpawnerBlockEntity_spawner_offset; // Field offset for modifying SpawnerBlockEntity's spawner field

    private static Field field_AbstractVillager_offers; // Field to get the offers of an AbstractVillager, normally private

    static {
        try {
            method_WorldServer_registerEntity = ReflectionUtils.getMethodByName(WorldServer.class, "registerEntity", Entity.class);

            Field field_EntityCreeper_d = ReflectionUtils.getFieldByName(EntityCreeper.class, "d");
            value_EntityCreeper_d = (DataWatcherObject<Boolean>) field_EntityCreeper_d.get(null);
            field_EntityCreeper_fuseTicks = ReflectionUtils.getFieldByName(EntityCreeper.class, "fuseTicks");

            field_PathfinderGoalSelector_d = ReflectionUtils.getFieldByName(PathfinderGoalSelector.class, "d");
            field_EntityInsentient_lookController = ReflectionUtils.getFieldByName(EntityInsentient.class, "lookController");
            field_EntityInsentient_moveController = ReflectionUtils.getFieldByName(EntityInsentient.class, "moveController");
            field_EntityInsentient_jumpController = ReflectionUtils.getFieldByName(EntityInsentient.class, "bi");
            field_EntityLiving_behaviorController = ReflectionUtils.getFieldByName(EntityLiving.class, "bg");

            if (NMSUtil.isPaper())
                field_Entity_spawnReason = ReflectionUtils.getFieldByPositionAndType(Entity.class, 0, SpawnReason.class);
            entityCounter = (AtomicInteger) ReflectionUtils.getFieldByName(Entity.class, "entityCount").get(null);

            Field field_SpawnerBlockEntity_spawner = ReflectionUtils.getFieldByPositionAndType(TileEntityMobSpawner.class, 0, MobSpawnerAbstract.class);
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
            field_SpawnerBlockEntity_spawner_offset = unsafe.objectFieldOffset(field_SpawnerBlockEntity_spawner);

            field_AbstractVillager_offers = ReflectionUtils.getFieldByName(EntityVillagerAbstract.class, "trades");
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

        EntityTypes<? extends Entity> nmsEntityType = IRegistry.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(entityType.getKey()));
        Entity nmsEntity = this.createCreature(
                nmsEntityType,
                ((CraftWorld) world).getHandle(),
                null,
                null,
                null,
                new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                this.toNmsSpawnReason(spawnReason)
        );

        return nmsEntity == null ? null : (LivingEntity) nmsEntity.getBukkitEntity();
    }

    /**
     * Duplicate of {@link EntityTypes#createCreature(WorldServer, NBTTagCompound, IChatBaseComponent, EntityHuman, BlockPosition, EnumMobSpawn, boolean, boolean)}.
     * Contains a patch to prevent chicken jockeys from spawning and to not play the mob sound upon creation.
     */
    public <T extends Entity> T createCreature(EntityTypes<T> entityTypes, WorldServer worldserver, NBTTagCompound nbttagcompound, IChatBaseComponent ichatbasecomponent, EntityHuman entityhuman, BlockPosition blockposition, EnumMobSpawn enummobspawn) {
        T newEntity;
        if (entityTypes == EntityTypes.SPIDER) {
            newEntity = (T) new SoloEntitySpider((EntityTypes<? extends EntitySpider>) entityTypes, worldserver);
        } else if (entityTypes == EntityTypes.STRIDER) {
            newEntity = (T) new SoloEntityStrider((EntityTypes<? extends EntityStrider>) entityTypes, worldserver);
        } else {
            newEntity = entityTypes.a(worldserver);
        }

        if (newEntity == null) {
            return null;
        } else {
            if (field_Entity_spawnReason != null) {
                try {
                    field_Entity_spawnReason.set(newEntity, this.toBukkitSpawnReason(enummobspawn));
                } catch (IllegalAccessException ignored) {}
            }

            newEntity.setPositionRotation(blockposition.getX() + 0.5D, blockposition.getY(), blockposition.getZ() + 0.5D, MathHelper.g(worldserver.random.nextFloat() * 360.0F), 0.0F);
            if (newEntity instanceof EntityInsentient entityinsentient) {
                entityinsentient.aC = entityinsentient.yaw;
                entityinsentient.aA = entityinsentient.yaw;

                GroupDataEntity groupDataEntity = null;
                if (entityTypes == EntityTypes.DROWNED
                        || entityTypes == EntityTypes.HUSK
                        || entityTypes == EntityTypes.ZOMBIE_VILLAGER
                        || entityTypes == EntityTypes.ZOMBIFIED_PIGLIN
                        || entityTypes == EntityTypes.ZOMBIE) {
                    // Don't allow chicken jockeys to spawn
                    groupDataEntity = new EntityZombie.GroupDataZombie(EntityZombie.a(worldserver.getRandom()), false);
                }

                entityinsentient.prepare(worldserver, worldserver.getDamageScaler(entityinsentient.getChunkCoordinates()), enummobspawn, groupDataEntity, nbttagcompound);
            }

            if (ichatbasecomponent != null && newEntity instanceof EntityLiving) {
                newEntity.setCustomName(ichatbasecomponent);
            }

            try {
                EntityTypes.a(worldserver, entityhuman, newEntity, nbttagcompound);
            } catch (Throwable ignored) {}

            return newEntity;
        }
    }

    @Override
    public void spawnExistingEntity(LivingEntity entity, SpawnReason spawnReason, boolean bypassSpawnEvent) {
        Location location = entity.getLocation();
        World world = location.getWorld();
        if (world == null)
            throw new IllegalArgumentException("Entity is not in a loaded world");

        if (bypassSpawnEvent) {
            IChunkAccess ichunkaccess = ((CraftWorld) world).getHandle().getChunkAt(MathHelper.floor(entity.getLocation().getX() / 16.0D), MathHelper.floor(entity.getLocation().getZ() / 16.0D), ChunkStatus.FULL, false);
            if (!(ichunkaccess instanceof Chunk))
                return;

            ichunkaccess.a(((CraftEntity) entity).getHandle());
            ((CraftWorld) world).getHandle().addEntityChunk(((CraftEntity) entity).getHandle());
        } else {
            ((CraftWorld) world).addEntity(((CraftEntity) entity).getHandle(), spawnReason);
        }
    }

    @Override
    public void updateEntityNameTagForPlayer(Player player, org.bukkit.entity.Entity entity, String customName, boolean customNameVisible) {
        try {
            List<DataWatcher.Item<?>> dataWatchers = new ArrayList<>();
            Optional<IChatBaseComponent> nameComponent = Optional.ofNullable(CraftChatMessage.fromStringOrNull(customName));
            dataWatchers.add(new DataWatcher.Item<>(DataWatcherRegistry.f.a(2), nameComponent));
            dataWatchers.add(new DataWatcher.Item<>(DataWatcherRegistry.i.a(3), customNameVisible));

            PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entity.getEntityId(), new DataWatcherWrapper(dataWatchers), false);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutEntityMetadata);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateEntityNameTagVisibilityForPlayer(Player player, org.bukkit.entity.Entity entity, boolean customNameVisible) {
        try {
            List<DataWatcher.Item<?>> dataItems = Lists.newArrayList(new DataWatcher.Item<>(DataWatcherRegistry.i.a(3), customNameVisible));
            PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entity.getEntityId(), new DataWatcherWrapper(dataItems), false);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutEntityMetadata);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unigniteCreeper(Creeper creeper) {
        EntityCreeper entityCreeper = ((CraftCreeper) creeper).getHandle();

        entityCreeper.getDataWatcher().set(value_EntityCreeper_d, false);
        try {
            field_EntityCreeper_fuseTicks.set(entityCreeper, entityCreeper.maxFuseTicks);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeEntityGoals(LivingEntity livingEntity) {
        EntityLiving nmsEntity = ((CraftLivingEntity) livingEntity).getHandle();
        if (!(nmsEntity instanceof EntityInsentient))
            return;

        try {
            EntityInsentient insentient = (EntityInsentient) nmsEntity;

            // Remove all goal AI other than floating in water
            Set<PathfinderGoalWrapped> goals = (Set<PathfinderGoalWrapped>) field_PathfinderGoalSelector_d.get(insentient.goalSelector);
            Iterator<PathfinderGoalWrapped> goalsIterator = goals.iterator();
            while (goalsIterator.hasNext()) {
                PathfinderGoalWrapped goal = goalsIterator.next();
                if (goal.j() instanceof PathfinderGoalFloat)
                    continue;

                goalsIterator.remove();
            }

            // Remove all targetting AI
            ((Set<PathfinderGoalWrapped>) field_PathfinderGoalSelector_d.get(insentient.targetSelector)).clear();

            // Forget any existing targets
            insentient.setGoalTarget(null);

            // Remove controllers
            field_EntityInsentient_lookController.set(insentient, new ControllerLook(insentient) {
                public void a() {}
            });
            field_EntityInsentient_moveController.set(insentient, new ControllerMove(insentient) {
                public void a() {}
            });
            if (!(insentient instanceof EntityRabbit)) {
                field_EntityInsentient_jumpController.set(insentient, new ControllerJump(insentient) {
                    public void b() {}
                });
            }
            field_EntityLiving_behaviorController.set(insentient, new BehaviorController(List.of(), List.of(), ImmutableList.of(), () -> BehaviorController.b(List.of(), List.of())));
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ItemStack setItemStackNBT(ItemStack itemStack, String key, String value) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();
        tagCompound.setString(key, value);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public ItemStack setItemStackNBT(ItemStack itemStack, String key, int value) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();
        tagCompound.setInt(key, value);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public String getItemStackNBTString(ItemStack itemStack, String key) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();
        return tagCompound.getString(key);
    }

    @Override
    public int getItemStackNBTInt(ItemStack itemStack, String key) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();
        return tagCompound.getInt(key);
    }

    @Override
    public String getItemStackNBTStringFromCompound(ItemStack itemStack, String compoundKey, String valueKey) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();
        NBTTagCompound targetCompound = tagCompound.getCompound(compoundKey);
        if (targetCompound == null)
            return "";
        return targetCompound.getString(valueKey);
    }

    @Override
    public void setLastHurtBy(LivingEntity livingEntity, Player player) {
        if (player != null)
            ((CraftLivingEntity) livingEntity).getHandle().killer = ((CraftPlayer) player).getHandle();
    }

    @Override
    public boolean hasLineOfSight(LivingEntity entity1, Location location) {
        EntityLiving nmsEntity1 = ((CraftLivingEntity) entity1).getHandle();
        Vec3D vec3d = new Vec3D(nmsEntity1.locX(), nmsEntity1.getHeadY(), nmsEntity1.locZ());
        Vec3D target = new Vec3D(location.getX(), location.getY(), location.getZ());
        return nmsEntity1.world.rayTrace(new RayTrace(vec3d, target, RayTrace.BlockCollisionOption.VISUAL, RayTrace.FluidCollisionOption.NONE, nmsEntity1)).getType() == MovingObjectPosition.EnumMovingObjectType.MISS;
    }

    @Override
    public boolean isActiveRaider(LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle() instanceof EntityRaider raider && raider.fa() != null;
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
        WorldServer level = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        TileEntity blockEntity = level.getTileEntity(blockPos);
        if (!(blockEntity instanceof TileEntityMobSpawner spawnerBlockEntity))
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
        return ((CraftWorld) item.getWorld()).getHandle().spigotConfig.itemDespawnRate;
    }

    @Override
    public List<ItemStack> getBoxContents(Item item) {
        ItemStack itemStack = item.getItemStack();

        // TODO
        return new ArrayList<>();
    }

    private SpawnReason toBukkitSpawnReason(EnumMobSpawn mobSpawnType) {
        return switch (mobSpawnType) {
            case SPAWN_EGG -> SpawnReason.SPAWNER_EGG;
            case SPAWNER -> SpawnReason.SPAWNER;
            default -> SpawnReason.CUSTOM;
        };
    }

    private EnumMobSpawn toNmsSpawnReason(SpawnReason spawnReason) {
        return switch (spawnReason) {
            case SPAWNER_EGG -> EnumMobSpawn.SPAWN_EGG;
            case SPAWNER -> EnumMobSpawn.SPAWNER;
            default -> EnumMobSpawn.COMMAND;
        };
    }

    public void saveEntityToTag(LivingEntity livingEntity, NBTTagCompound compoundTag) {
        // Async villager "fix", if the trades aren't loaded yet force them to save as empty, they will get loaded later
        if (livingEntity instanceof AbstractVillager) {
            try {
                EntityVillagerAbstract villager = ((CraftAbstractVillager) livingEntity).getHandle();

                // Set the trades to empty if they are null to prevent trades from generating during the saveWithoutId call
                boolean bypassTrades = field_AbstractVillager_offers.get(villager) == null;
                if (bypassTrades)
                    field_AbstractVillager_offers.set(villager, new MerchantRecipeList());

                ((CraftLivingEntity) livingEntity).getHandle().save(compoundTag);

                // Restore the offers back to null and make sure nothing is written to the NBT
                if (bypassTrades) {
                    field_AbstractVillager_offers.set(villager, null);
                    compoundTag.remove("Offers");
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else {
            ((CraftLivingEntity) livingEntity).getHandle().save(compoundTag);
        }
    }

    public void registerEntity(WorldServer world, Entity entity) {
        try {
            method_WorldServer_registerEntity.invoke(world, entity);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
