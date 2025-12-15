package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.scheduler.task.ScheduledTask;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.stack.StackingThread;
import dev.rosewood.rosestacker.utils.VersionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BoundingBox;

public class EntityCacheManager extends Manager {

    private static final boolean DIRECT_GETTERS = NMSUtil.isPaper() && (NMSUtil.getVersionNumber() > 20 || (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 4));

    private final Map<ChunkLocation, Collection<Entity>> entityCache;
    private ScheduledTask refreshTask;

    public EntityCacheManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.entityCache = new ConcurrentHashMap<>();
    }

    @Override
    public void reload() {
        this.refreshTask = this.rosePlugin.getScheduler().runTaskTimer(this::refresh, 5L, 60L);
    }

    @Override
    public void disable() {
        this.entityCache.clear();

        if (this.refreshTask != null) {
            this.refreshTask.cancel();
            this.refreshTask = null;
        }
    }

    /**
     * Gets nearby entities from cache
     *
     * @param center The center of the area to check
     * @param radius The radius to check around
     * @param predicate Conditions to be met
     * @return A Set of nearby entities
     */
    public Collection<Entity> getNearbyEntities(Location center, double radius, Predicate<Entity> predicate) {
        Set<Entity> nearbyEntities = new HashSet<>();
        World world = center.getWorld();
        if (world == null)
            return nearbyEntities;

        BoundingBox boundingBox = new BoundingBox(
                center.getX() - radius,
                center.getY() - radius,
                center.getZ() - radius,
                center.getX() + radius,
                center.getY() + radius,
                center.getZ() + radius
        );

        int minX = (int) boundingBox.getMinX() >> 4;
        int maxX = (int) boundingBox.getMaxX() >> 4;
        int minY = (int) boundingBox.getMinY() >> 4;
        int maxY = (int) boundingBox.getMaxY() >> 4;
        int minZ = (int) boundingBox.getMinZ() >> 4;
        int maxZ = (int) boundingBox.getMaxZ() >> 4;

        Location location = center.clone(); // re-use location object to dump positions so we aren't constantly remaking Location objects
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Collection<Entity> entities = this.entityCache.get(new ChunkLocation(world.getName(), x, y, z));
                    if (entities == null)
                        continue;

                    this.filter(location, boundingBox, entities, predicate, nearbyEntities);
                }
            }
        }

        return nearbyEntities;
    }

    private void filter(Location location, BoundingBox boundingBox, Collection<Entity> entities, Predicate<Entity> predicate, Set<Entity> collector) {
        if (DIRECT_GETTERS) {
            for (Entity entity : entities) {
                if (boundingBox.contains(entity.getX(), entity.getY(), entity.getZ())
                        && predicate.test(entity)
                        && entity.isValid())
                    collector.add(entity);
            }
        } else {
            for (Entity entity : entities) {
                entity.getLocation(location);
                if (boundingBox.contains(location.getX(), location.getY(), location.getZ())
                        && predicate.test(entity)
                        && entity.isValid())
                    collector.add(entity);
            }
        }
    }

    /**
     * Gets entities in the Chunk of a Location
     *
     * @param location The Location of the Chunk
     * @param predicate Conditions to be met
     * @return A Set of entities in the chunk
     */
    public Collection<Entity> getEntitiesInChunk(Location location, Predicate<Entity> predicate) {
        World world = location.getWorld();
        if (world == null)
            return new ArrayList<>();

        int x = location.getBlockX() >> 4;
        int z = location.getBlockZ() >> 4;
        int minY = world.getMinHeight() >> 4;
        int maxY = world.getMaxHeight() >> 4;

        Set<Entity> entities = new HashSet<>();
        for (int y = minY; y <= maxY; y++) {
            Collection<Entity> chunkEntities = this.entityCache.get(new ChunkLocation(world.getName(), x, y, z));
            if (chunkEntities != null)
                entities.addAll(chunkEntities);
        }

        Set<Entity> nearbyEntities = new HashSet<>();
        for (Entity entity : entities)
            if (predicate.test(entity) && entity.isValid())
                nearbyEntities.add(entity);

        return nearbyEntities;
    }

    /**
     * Forces an entry into the cache, used for newly spawned entities
     *
     * @param entity The entity to cache
     */
    public void preCacheEntity(Entity entity) {
        Location location = entity.getLocation();
        ChunkLocation chunkLocation = new ChunkLocation(entity.getWorld().getName(), (int) location.getX() >> 4, (int) location.getY() >> 4, (int) location.getZ() >> 4);
        Collection<Entity> entities = this.entityCache.computeIfAbsent(chunkLocation, k -> this.createCollection());
        entities.add(entity);
    }

    private void refresh() {
        synchronized (this.entityCache) {
            this.entityCache.clear();
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            for (StackingThread stackingThread : this.rosePlugin.getManager(StackManager.class).getStackingThreads().values()) {
                World world = stackingThread.getTargetWorld();
                this.addWorldEntities(world, nmsHandler.getEntities(world));
            }
        }
    }

    private void addWorldEntities(World world, List<Entity> worldEntities) {
        if (DIRECT_GETTERS) {
            for (Entity entity : worldEntities) {
                EntityType type = entity.getType();
                if (type != VersionUtils.ITEM && (!type.isAlive() || type == EntityType.PLAYER || type == EntityType.ARMOR_STAND))
                    continue;

                ChunkLocation chunkLocation = new ChunkLocation(world.getName(), (int) entity.getX() >> 4, (int) entity.getY() >> 4, (int) entity.getZ() >> 4);
                Collection<Entity> entities = this.entityCache.computeIfAbsent(chunkLocation, k -> this.createCollection());
                entities.add(entity);
            }
        } else {
            Location location = new Location(world, 0, 0, 0);
            for (Entity entity : worldEntities) {
                EntityType type = entity.getType();
                if (type != VersionUtils.ITEM && (!type.isAlive() || type == EntityType.PLAYER || type == EntityType.ARMOR_STAND))
                    continue;

                entity.getLocation(location); // re-use location object to dump positions so we aren't constantly remaking Location objects
                ChunkLocation chunkLocation = new ChunkLocation(world.getName(), (int) location.getX() >> 4, (int) location.getY() >> 4, (int) location.getZ() >> 4);
                Collection<Entity> entities = this.entityCache.computeIfAbsent(chunkLocation, k -> this.createCollection());
                entities.add(entity);
            }
        }
    }

    private Collection<Entity> createCollection() {
        return new LinkedBlockingDeque<>();
    }

    /**
     * Represents a 16x16x16 chunk in the world
     */
    private record ChunkLocation(String world, int x, int y, int z) { }

}
