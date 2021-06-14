package dev.rosewood.rosestacker.nms.v1_17_R1;

import com.google.common.collect.Lists;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.object.SpawnerTileWrapper;
import dev.rosewood.rosestacker.nms.util.ReflectionUtils;
import dev.rosewood.rosestacker.nms.v1_17_R1.entity.SoloEntitySpider;
import dev.rosewood.rosestacker.nms.v1_17_R1.entity.SoloEntityStrider;
import dev.rosewood.rosestacker.nms.v1_17_R1.object.SpawnerTileWrapperImpl;
import dev.rosewood.rosestacker.nms.v1_17_R1.object.SynchedEntityDataWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
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
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unchecked")
public class NMSHandlerImpl implements NMSHandler {

    private static EntityDataAccessor<Boolean> value_Creeper_DATA_IS_IGNITED; // DataWatcherObject that determines if a creeper is ignited, normally private

    private static Field field_GoalSelector_availableGoals; // Field to get the available pathing goals of a mob, normally private
    private static Field field_Mob_moveControl; // Field to set the move controller of a mob, normally protected

    private static Field field_ServerLevel_entityManager; // Field to get the persistent entity section manager, normally private

    static {
        try {
            Field field_Creeper_DATA_IS_IGNITED = ReflectionUtils.getFieldByPositionAndType(net.minecraft.world.entity.monster.Creeper.class, 2, EntityDataAccessor.class);
            value_Creeper_DATA_IS_IGNITED = (EntityDataAccessor<Boolean>) field_Creeper_DATA_IS_IGNITED.get(null);
            field_GoalSelector_availableGoals = ReflectionUtils.getFieldByPositionAndType(GoalSelector.class, 0, Set.class);
            field_Mob_moveControl = ReflectionUtils.getFieldByPositionAndType(Mob.class, 0, MoveControl.class);
            field_ServerLevel_entityManager = ReflectionUtils.getFieldByPositionAndType(ServerLevel.class, 0, PersistentEntitySectionManager.class);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getEntityAsNBT(LivingEntity livingEntity, boolean includeAttributes) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            CompoundTag nbt = new CompoundTag();
            net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) livingEntity).getHandle();
            nmsEntity.save(nbt);

            // Don't store attributes, it's pretty large and doesn't usually matter
            if (!includeAttributes)
                nbt.remove("Attributes");

            // Write entity type
            String entityType = Registry.ENTITY_TYPE.getKey(nmsEntity.getType()).toString();
            dataOutput.writeUTF(entityType);

            // Write NBT
            NbtIo.writeCompressed(nbt, dataOutput);

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public LivingEntity createEntityFromNBT(byte[] serialized, Location location, boolean addToWorld, EntityType overwriteType) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(serialized);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read entity type
            String entityType = dataInput.readUTF();
            if (overwriteType != null)
                entityType = overwriteType.getKey().getKey();

            // Read NBT
            CompoundTag nbt = NbtIo.readCompressed(dataInput);

            ListTag positionTagList = nbt.getList("Pos", 6);
            positionTagList.set(0, DoubleTag.valueOf(location.getX()));
            positionTagList.set(1, DoubleTag.valueOf(location.getY()));
            positionTagList.set(2, DoubleTag.valueOf(location.getZ()));
            nbt.put("Pos", positionTagList);
            nbt.putUUID("UUID", UUID.randomUUID()); // Reset the UUID to resolve possible duplicates

            Optional<net.minecraft.world.entity.EntityType<?>> optionalEntity = net.minecraft.world.entity.EntityType.byString(entityType);
            if (optionalEntity.isPresent()) {
                ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();

                Entity entity = this.createCreature(
                        optionalEntity.get(),
                        world,
                        nbt,
                        null,
                        null,
                        new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                        MobSpawnType.COMMAND
                );

                if (entity == null)
                    throw new NullPointerException("Unable to create entity from NBT");

                if (addToWorld)
                    world.addWithUUID(entity);

                // Load NBT
                entity.load(nbt);

                return (LivingEntity) entity.getBukkitEntity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public LivingEntity createNewEntityUnspawned(EntityType entityType, Location location) {
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
                MobSpawnType.SPAWN_EGG
        );

        return nmsEntity == null ? null : (LivingEntity) nmsEntity.getBukkitEntity();
    }

    /**
     * Duplicate of {@link net.minecraft.world.entity.EntityType#create(ServerLevel, CompoundTag, Component, net.minecraft.world.entity.player.Player, BlockPos, MobSpawnType, boolean, boolean)}.
     * Contains a patch to prevent chicken jockeys from spawning and to not play the mob sound upon creation.
     */
    private <T extends Entity> T createCreature(net.minecraft.world.entity.EntityType<T> entityType, ServerLevel world, CompoundTag nbt, Component component, net.minecraft.world.entity.player.Player player, BlockPos blockPos, MobSpawnType mobSpawnType) {
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

        newEntity.moveTo(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D, Mth.wrapDegrees(world.random.nextFloat() * 360.0F), 0.0F);
        if (newEntity instanceof Mob) {
            Mob mob = (Mob) newEntity;
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
    public void spawnExistingEntity(LivingEntity entity, SpawnReason spawnReason) {
        Location location = entity.getLocation();
        World world = location.getWorld();
        if (world == null)
            throw new IllegalArgumentException("Entity is not in a loaded world");

        ((CraftWorld) world).getHandle().addEntity(((CraftEntity) entity).getHandle(), spawnReason);
    }

    @Override
    public LivingEntity spawnEntityWithReason(EntityType entityType, Location location, SpawnReason spawnReason) {
        World world = location.getWorld();
        if (world == null)
            throw new IllegalArgumentException("Cannot spawn into null world");

        Class<? extends org.bukkit.entity.Entity> entityClass = entityType.getEntityClass();
        if (entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass))
            throw new IllegalArgumentException("EntityType must be of a LivingEntity");

        CraftWorld craftWorld = (CraftWorld) world;
        return (LivingEntity) craftWorld.spawn(location, entityClass, null, spawnReason);
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

            // Remove the move controller and replace it with a dummy one
            MoveControl dummyMoveController = new MoveControl(mob) {
                @Override
                public void tick() { }
            };

            field_Mob_moveControl.set(mob, dummyMoveController);
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
    public SpawnerTileWrapper getSpawnerTile(CreatureSpawner spawner) {
        return new SpawnerTileWrapperImpl(spawner);
    }

    @Override
    public List<org.bukkit.entity.Entity> getEntities(World world) {
        try {
            ServerLevel level = ((CraftWorld) world).getHandle();
            PersistentEntitySectionManager<Entity> entityManager = (PersistentEntitySectionManager<Entity>) field_ServerLevel_entityManager.get(level);
            List<org.bukkit.entity.Entity> entities = new ArrayList<>();
            for (Entity entity : entityManager.getEntityGetter().getAll()) {
                if (entity == null)
                    continue;

                org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
                if (bukkitEntity != null && bukkitEntity.isValid())
                    entities.add(bukkitEntity);
            }
            return entities;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<org.bukkit.entity.Entity> getEntities(Chunk chunk) {
        try {
            Location location = new Location(null, 0, 0, 0);
            return this.getEntities(chunk.getWorld()).stream().filter((entity) -> {
                entity.getLocation(location);
                return location.getBlockX() >> 4 == chunk.getX() && location.getBlockZ() >> 4 == chunk.getZ();
            }).collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

}
