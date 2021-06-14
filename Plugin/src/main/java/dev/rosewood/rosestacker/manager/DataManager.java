package dev.rosewood.rosestacker.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.SQLiteConnector;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.EntitySerializer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

public class DataManager extends AbstractDataManager {

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    public void getStackedEntities(Set<Chunk> chunks, Consumer<Set<StackedEntity>> callback) {
        if (chunks.isEmpty())
            callback.accept(Collections.emptySet());

        String queryTemplate = "SELECT * FROM " + this.getTablePrefix() + "stacked_entity WHERE world = '%s' AND chunk_x = %d AND chunk_z = %d";
        List<String> queries = new ArrayList<>();

        int count = 0;
        StringBuilder compoundSelect = new StringBuilder();
        Map<UUID, Entity> chunkEntities = new HashMap<>();
        Iterator<Chunk> chunkIterator = chunks.iterator();
        while (chunkIterator.hasNext()) {
            Chunk chunk = chunkIterator.next();
            try {
                for (Entity entity : NMSAdapter.getHandler().getEntities(chunk))
                    chunkEntities.put(entity.getUniqueId(), entity);
            } catch (Exception ignored) { }

            if (compoundSelect.length() > 0)
                compoundSelect.append(" UNION ALL ");
            compoundSelect.append(String.format(queryTemplate, chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));

            if (++count >= 500 || !chunkIterator.hasNext()) {
                queries.add(compoundSelect.toString());
                compoundSelect.setLength(0);
                count = 0;
            }
        }

        Set<StackedEntityData> stackedEntityData = new HashSet<>();
        this.databaseConnector.connect(connection -> {
            for (String query : queries) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet result = statement.executeQuery(query);
                    while (result.next()) {
                        stackedEntityData.add(new StackedEntityData(
                                result.getInt("id"),
                                UUID.fromString(result.getString("entity_uuid")),
                                result.getBytes("stack_entities")
                        ));
                    }
                }
            }
        });

        Runnable task = () -> {
            Set<StackedEntity> stackedEntities = new HashSet<>();
            Set<Stack<?>> cleanup = new HashSet<>();

            for (StackedEntityData stackData : stackedEntityData) {
                Entity entity = chunkEntities.get(stackData.entityUUID);
                if (entity != null) {
                    stackedEntities.add(EntitySerializer.fromBlob(stackData.id, (LivingEntity) entity, stackData.stackEntities));
                } else {
                    cleanup.add(new StackedEntity(stackData.id, null, null));
                }
            }

            callback.accept(stackedEntities);

            if (!cleanup.isEmpty())
                Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.deleteStacks(cleanup));
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, task);
        }
    }

    public void getStackedItems(Set<Chunk> chunks, Consumer<Set<StackedItem>> callback) {
        if (chunks.isEmpty())
            callback.accept(Collections.emptySet());

        String queryTemplate = "SELECT * FROM " + this.getTablePrefix() + "stacked_item WHERE world = '%s' AND chunk_x = %d AND chunk_z = %d";
        List<String> queries = new ArrayList<>();

        int count = 0;
        StringBuilder compoundSelect = new StringBuilder();
        Map<UUID, Entity> chunkEntities = new HashMap<>();
        Iterator<Chunk> chunkIterator = chunks.iterator();
        while (chunkIterator.hasNext()) {
            Chunk chunk = chunkIterator.next();
            try {
                for (Entity entity : NMSAdapter.getHandler().getEntities(chunk))
                    chunkEntities.put(entity.getUniqueId(), entity);
            } catch (Exception ignored) { }

            if (compoundSelect.length() > 0)
                compoundSelect.append(" UNION ALL ");
            compoundSelect.append(String.format(queryTemplate, chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));

            if (++count >= 500 || !chunkIterator.hasNext()) {
                queries.add(compoundSelect.toString());
                compoundSelect.setLength(0);
                count = 0;
            }
        }

        Set<StackedItemData> stackedItemData = new HashSet<>();
        this.databaseConnector.connect(connection -> {
            for (String query : queries) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet result = statement.executeQuery(query);
                    while (result.next()) {
                        stackedItemData.add(new StackedItemData(
                                result.getInt("id"),
                                result.getInt("stack_size"),
                                UUID.fromString(result.getString("entity_uuid"))
                        ));
                    }
                }
            }
        });

        Runnable task = () -> {
            Set<StackedItem> stackedItems = new HashSet<>();
            Set<Stack<?>> cleanup = new HashSet<>();

            for (StackedItemData stackData : stackedItemData) {
                Entity entity = chunkEntities.get(stackData.entityUUID);
                if (entity != null) {
                    stackedItems.add(new StackedItem(stackData.id, stackData.stackSize, (Item) entity));
                } else {
                    cleanup.add(new StackedItem(stackData.id, 0, null));
                }
            }

            callback.accept(stackedItems);

            if (!cleanup.isEmpty())
                Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.deleteStacks(cleanup));
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, task);
        }
    }

    public void getStackedBlocks(Set<Chunk> chunks, Consumer<Set<StackedBlock>> callback) {
        if (chunks.isEmpty())
            callback.accept(Collections.emptySet());

        String queryTemplate = "SELECT * FROM " + this.getTablePrefix() + "stacked_block WHERE world = '%s' AND chunk_x = %d AND chunk_z = %d";
        List<String> queries = new ArrayList<>();

        int count = 0;
        StringBuilder compoundSelect = new StringBuilder();
        Iterator<Chunk> chunkIterator = chunks.iterator();
        while (chunkIterator.hasNext()) {
            Chunk chunk = chunkIterator.next();
            if (compoundSelect.length() > 0)
                compoundSelect.append(" UNION ALL ");
            compoundSelect.append(String.format(queryTemplate, chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));

            if (++count >= 500 || !chunkIterator.hasNext()) {
                queries.add(compoundSelect.toString());
                compoundSelect.setLength(0);
                count = 0;
            }
        }

        Set<StackedBlockData> stackedBlockData = new HashSet<>();
        this.databaseConnector.connect(connection -> {
            for (String query : queries) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet result = statement.executeQuery(query);
                    while (result.next()) {
                        stackedBlockData.add(new StackedBlockData(
                                result.getInt("id"),
                                result.getInt("stack_size"),
                                result.getInt("chunk_x"),
                                result.getInt("chunk_z"),
                                result.getInt("block_x"),
                                result.getInt("block_y"),
                                result.getInt("block_z"),
                                result.getString("world")
                        ));
                    }
                }
            }
        });

        Runnable task = () -> {
            Set<StackedBlock> stackedBlocks = new HashSet<>();
            Set<Stack<?>> cleanup = new HashSet<>();

            for (StackedBlockData stackData : stackedBlockData) {
                World world = Bukkit.getWorld(stackData.world);
                Block block = null;

                boolean invalid = world == null;
                if (!invalid) {
                    block = world.getBlockAt((stackData.chunkX << 4) + stackData.blockX, stackData.blockY, (stackData.chunkZ << 4) + stackData.blockZ);
                    if (block.getType() == Material.AIR)
                        invalid = true;
                }

                if (!invalid) {
                    stackedBlocks.add(new StackedBlock(stackData.id, stackData.stackSize, block));
                } else {
                    cleanup.add(new StackedBlock(stackData.id, 0, null));
                }
            }

            callback.accept(stackedBlocks);

            if (!cleanup.isEmpty())
                Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.deleteStacks(cleanup));
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, task);
        }
    }

    public void getStackedSpawners(Set<Chunk> chunks, Consumer<Set<StackedSpawner>> callback) {
        if (chunks.isEmpty())
            callback.accept(Collections.emptySet());

        String queryTemplate = "SELECT * FROM " + this.getTablePrefix() + "stacked_spawner WHERE world = '%s' AND chunk_x = %d AND chunk_z = %d";
        List<String> queries = new ArrayList<>();

        int count = 0;
        StringBuilder compoundSelect = new StringBuilder();
        Iterator<Chunk> chunkIterator = chunks.iterator();
        while (chunkIterator.hasNext()) {
            Chunk chunk = chunkIterator.next();
            if (compoundSelect.length() > 0)
                compoundSelect.append(" UNION ALL ");
            compoundSelect.append(String.format(queryTemplate, chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));

            if (++count >= 500 || !chunkIterator.hasNext()) {
                queries.add(compoundSelect.toString());
                compoundSelect.setLength(0);
                count = 0;
            }
        }

        Set<StackedSpawnerData> stackedSpawnerData = new HashSet<>();
        this.databaseConnector.connect(connection -> {
            for (String query : queries) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet result = statement.executeQuery(query);
                    while (result.next()) {
                        stackedSpawnerData.add(new StackedSpawnerData(
                                result.getInt("id"),
                                result.getInt("stack_size"),
                                result.getInt("chunk_x"),
                                result.getInt("chunk_z"),
                                result.getInt("block_x"),
                                result.getInt("block_y"),
                                result.getInt("block_z"),
                                result.getString("world"),
                                result.getBoolean("placed_by_player")
                        ));
                    }
                }
            }
        });

        Runnable task = () -> {
            Set<StackedSpawner> stackedSpawners = new HashSet<>();
            Set<Stack<?>> cleanup = new HashSet<>();

            for (StackedSpawnerData stackData : stackedSpawnerData) {
                World world = Bukkit.getWorld(stackData.world);
                Block block = null;

                boolean invalid = world == null;
                if (!invalid) {
                    block = world.getBlockAt((stackData.chunkX << 4) + stackData.blockX, stackData.blockY, (stackData.chunkZ << 4) + stackData.blockZ);
                    if (block.getType() != Material.SPAWNER)
                        invalid = true;
                }

                if (!invalid) {
                    stackedSpawners.add(new StackedSpawner(stackData.id, stackData.stackSize, (CreatureSpawner) block.getState(), stackData.placedByPlayer));
                } else {
                    cleanup.add(new StackedBlock(stackData.id, 0, null));
                }
            }

            callback.accept(stackedSpawners);

            if (!cleanup.isEmpty())
                Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.deleteStacks(cleanup));
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(this.rosePlugin, task);
        }
    }

    public void createOrUpdateStackedEntities(Collection<StackedEntity> stackedEntities) {
        if (stackedEntities.isEmpty())
            return;

        this.databaseConnector.connect(connection -> {
            Set<StackedEntity> update = stackedEntities.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());
            Set<StackedEntity> insert = stackedEntities.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());

            if (!update.isEmpty()) {
                String batchUpdate = "UPDATE " + this.getTablePrefix() + "stacked_entity SET entity_uuid = ?, stack_entities = ?, world = ?, chunk_x = ?, chunk_z = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(batchUpdate)) {
                    for (StackedEntity stack : update) {
                        statement.setString(1, stack.getEntity().getUniqueId().toString());
                        statement.setBytes(2, EntitySerializer.toBlob(stack));
                        statement.setString(3, stack.getLocation().getWorld().getName());
                        statement.setInt(4, stack.getLocation().getBlockX() >> 4);
                        statement.setInt(5, stack.getLocation().getBlockZ() >> 4);
                        statement.setInt(6, stack.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + "stacked_entity (entity_uuid, stack_entities, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?, ?)";

                if (this.databaseConnector instanceof SQLiteConnector) {
                    batchInsert += " ON CONFLICT(entity_uuid) DO UPDATE SET stack_entities = ?, world = ?, chunk_x = ?, chunk_z = ?";
                } else {
                    batchInsert += " ON DUPLICATE KEY UPDATE stack_entities = ?, world = ?, chunk_x = ?, chunk_z = ?";
                }

                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (StackedEntity stack : insert) {
                        statement.setString(1, stack.getEntity().getUniqueId().toString());
                        statement.setBytes(2, EntitySerializer.toBlob(stack));
                        statement.setString(3, stack.getLocation().getWorld().getName());
                        statement.setInt(4, stack.getLocation().getBlockX() >> 4);
                        statement.setInt(5, stack.getLocation().getBlockZ() >> 4);
                        // On conflict
                        statement.setBytes(6, EntitySerializer.toBlob(stack));
                        statement.setString(7, stack.getLocation().getWorld().getName());
                        statement.setInt(8, stack.getLocation().getBlockX() >> 4);
                        statement.setInt(9, stack.getLocation().getBlockZ() >> 4);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void createOrUpdateStackedItems(Collection<StackedItem> stackedItems) {
        if (stackedItems.isEmpty())
            return;

        this.databaseConnector.connect(connection -> {
            Set<StackedItem> update = stackedItems.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());
            Set<StackedItem> insert = stackedItems.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());

            if (!update.isEmpty()) {
                String batchUpdate = "UPDATE " + this.getTablePrefix() + "stacked_item SET stack_size = ?, entity_uuid = ?, world = ?, chunk_x = ?, chunk_z = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(batchUpdate)) {
                    for (StackedItem stack : update) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setString(2, stack.getItem().getUniqueId().toString());
                        statement.setString(3, stack.getLocation().getWorld().getName());
                        statement.setInt(4, stack.getLocation().getBlockX() >> 4);
                        statement.setInt(5, stack.getLocation().getBlockZ() >> 4);
                        statement.setInt(6, stack.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + "stacked_item (stack_size, entity_uuid, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?, ?)";

                if (this.databaseConnector instanceof SQLiteConnector) {
                    batchInsert += " ON CONFLICT(entity_uuid) DO UPDATE SET stack_size = ?, world = ?, chunk_x = ?, chunk_z = ?";
                } else {
                    batchInsert += " ON DUPLICATE KEY UPDATE stack_size = ?, world = ?, chunk_x = ?, chunk_z = ?";
                }

                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (StackedItem stack : insert) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setString(2, stack.getItem().getUniqueId().toString());
                        statement.setString(3, stack.getLocation().getWorld().getName());
                        statement.setInt(4, stack.getLocation().getBlockX() >> 4);
                        statement.setInt(5, stack.getLocation().getBlockZ() >> 4);
                        // On conflict
                        statement.setInt(6, stack.getStackSize());
                        statement.setString(7, stack.getLocation().getWorld().getName());
                        statement.setInt(8, stack.getLocation().getBlockX() >> 4);
                        statement.setInt(9, stack.getLocation().getBlockZ() >> 4);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void createOrUpdateStackedBlocks(Collection<StackedBlock> stackedBlocks) {
        if (stackedBlocks.isEmpty())
            return;

        this.databaseConnector.connect(connection -> {
            Set<StackedBlock> update = stackedBlocks.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());
            Set<StackedBlock> insert = stackedBlocks.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());

            if (!update.isEmpty()) {
                String batchUpdate = "UPDATE " + this.getTablePrefix() + "stacked_block SET stack_size = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(batchUpdate)) {
                    for (StackedBlock stack : update) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setInt(2, stack.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + "stacked_block (stack_size, world, chunk_x, chunk_z, block_x, block_y, block_z) VALUES (?, ?, ?, ?, ?, ?, ?)";

                if (this.databaseConnector instanceof SQLiteConnector) {
                    batchInsert += " ON CONFLICT(world, chunk_x, chunk_z, block_x, block_y, block_z) DO UPDATE SET stack_size = ?";
                } else {
                    batchInsert += " ON DUPLICATE KEY UPDATE stack_size = ?";
                }

                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (StackedBlock stack : insert) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setString(2, stack.getLocation().getWorld().getName());
                        statement.setInt(3, stack.getLocation().getBlockX() >> 4);
                        statement.setInt(4, stack.getLocation().getBlockZ() >> 4);
                        statement.setInt(5, stack.getLocation().getBlockX() & 0xF);
                        statement.setInt(6, stack.getLocation().getBlockY());
                        statement.setInt(7, stack.getLocation().getBlockZ() & 0xF);
                        statement.setInt(8, stack.getStackSize());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void createOrUpdateStackedSpawners(Collection<StackedSpawner> stackedSpawners) {
        if (stackedSpawners.isEmpty())
            return;

        this.databaseConnector.connect(connection -> {
            Set<StackedSpawner> update = stackedSpawners.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());
            Set<StackedSpawner> insert = stackedSpawners.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());

            if (!update.isEmpty()) {
                String batchUpdate = "UPDATE " + this.getTablePrefix() + "stacked_spawner SET stack_size = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(batchUpdate)) {
                    for (StackedSpawner stack : update) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setInt(2, stack.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + "stacked_spawner (stack_size, world, chunk_x, chunk_z, block_x, block_y, block_z, placed_by_player) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                if (this.databaseConnector instanceof SQLiteConnector) {
                    batchInsert += " ON CONFLICT(world, chunk_x, chunk_z, block_x, block_y, block_z) DO UPDATE SET stack_size = ?";
                } else {
                    batchInsert += " ON DUPLICATE KEY UPDATE stack_size = ?";
                }

                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (StackedSpawner stack : insert) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setString(2, stack.getLocation().getWorld().getName());
                        statement.setInt(3, stack.getLocation().getBlockX() >> 4);
                        statement.setInt(4, stack.getLocation().getBlockZ() >> 4);
                        statement.setInt(5, stack.getLocation().getBlockX() & 0xF);
                        statement.setInt(6, stack.getLocation().getBlockY());
                        statement.setInt(7, stack.getLocation().getBlockZ() & 0xF);
                        statement.setBoolean(8, stack.isPlacedByPlayer());
                        statement.setInt(8, stack.getStackSize());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void deleteStacks(Set<Stack<?>> stacks) {
        if (stacks.isEmpty())
            return;

        Set<StackedBlock> stackedBlocks = new HashSet<>();
        Set<StackedEntity> stackedEntities = new HashSet<>();
        Set<StackedItem> stackedItems = new HashSet<>();
        Set<StackedSpawner> stackedSpawners = new HashSet<>();

        for (Stack<?> stack : stacks) {
            if (stack.getId() == -1)
                continue;

            if (stack instanceof StackedBlock) {
                stackedBlocks.add((StackedBlock) stack);
            } else if (stack instanceof StackedEntity) {
                stackedEntities.add((StackedEntity) stack);
            } else if (stack instanceof StackedItem) {
                stackedItems.add((StackedItem) stack);
            } else if (stack instanceof StackedSpawner) {
                stackedSpawners.add((StackedSpawner) stack);
            }
        }

        this.databaseConnector.connect(connection -> {
            if (!stackedBlocks.isEmpty())
                this.deleteStackBatch(connection, stackedBlocks, "stacked_block");

            if (!stackedEntities.isEmpty())
                this.deleteStackBatch(connection, stackedEntities, "stacked_entity");

            if (!stackedItems.isEmpty())
                this.deleteStackBatch(connection, stackedItems, "stacked_item");

            if (!stackedSpawners.isEmpty())
                this.deleteStackBatch(connection, stackedSpawners, "stacked_spawner");
        });
    }

    private <T extends Stack<?>> void deleteStackBatch(Connection connection, Set<T> stacks, String tableName) {
        String batchDelete = "DELETE FROM " + this.getTablePrefix() + tableName + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(batchDelete)) {
            for (Stack<?> stack : stacks) {
                statement.setInt(1, stack.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int purgeData(String world) {
        AtomicInteger totalDeleted = new AtomicInteger();
        this.databaseConnector.connect(connection -> {
            Set<String> types = Stream.of(StackType.values()).map(x -> "stacked_" + x.name().toLowerCase()).collect(Collectors.toSet());
            for (String type : types) {
                String delete = "DELETE FROM " + this.getTablePrefix() + type + " WHERE world = ?";
                try (PreparedStatement statement = connection.prepareStatement(delete)) {
                    statement.setString(1, world);
                    totalDeleted.addAndGet(statement.executeUpdate());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        return totalDeleted.get();
    }

    public StackCounts queryData(String world) {
        AtomicReference<StackCounts> stackCounts = new AtomicReference<>();
        this.databaseConnector.connect(connection -> stackCounts.set(new StackCounts(
                this.queryData(world, StackType.ENTITY, connection),
                this.queryData(world, StackType.ITEM, connection),
                this.queryData(world, StackType.BLOCK, connection),
                this.queryData(world, StackType.SPAWNER, connection)
        )));
        return stackCounts.get();
    }

    private int queryData(String world, StackType stackType, Connection connection) throws SQLException {
        AtomicInteger total = new AtomicInteger();
        String query = "SELECT COUNT(*) FROM " + this.getTablePrefix() + "stacked_" + stackType.name().toLowerCase() + " WHERE world = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, world);
            ResultSet result = statement.executeQuery();
            result.next();
            total.addAndGet(result.getInt(1));
        }
        return total.get();
    }

    public void setConversionHandlers(Set<ConverterType> converterTypes) {
        this.databaseConnector.connect(connection -> {
            String insertIgnore;
            if (this.databaseConnector instanceof SQLiteConnector) {
                insertIgnore = "INSERT OR IGNORE INTO ";
            } else {
                insertIgnore = "INSERT IGNORE ";
            }

            String query = insertIgnore + this.getTablePrefix() + "convert_handler (name) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (ConverterType converterType : converterTypes) {
                    statement.setString(1, converterType.name());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
    }

    public Set<ConverterType> getConversionHandlers() {
        Set<ConverterType> conversionHandlers = EnumSet.noneOf(ConverterType.class);
        this.databaseConnector.connect(connection -> {
            String query = "SELECT name FROM " + this.getTablePrefix() + "convert_handler";
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    ConverterType converterType = ConverterType.get(result.getString(1));
                    if (converterType != null)
                        conversionHandlers.add(converterType);
                }
            }
        });
        return conversionHandlers;
    }

    public void setConversionData(Map<StackType, Set<ConversionData>> conversionData) {
        if (conversionData.isEmpty())
            return;

        this.databaseConnector.connect(connection -> {
            Set<ConversionData> entityData = conversionData.get(StackType.ENTITY);
            if (entityData != null && !entityData.isEmpty()) {
                String entityInsert = "INSERT INTO " + this.getTablePrefix() + "convert_stacked_entity (entity_uuid, stack_size) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(entityInsert)) {
                    for (ConversionData data : entityData) {
                        statement.setString(1, data.getUniqueId().toString());
                        statement.setInt(2, data.getStackSize());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }

            Set<ConversionData> itemData = conversionData.get(StackType.ITEM);
            if (itemData != null && !itemData.isEmpty()) {
                String itemInsert = "INSERT INTO " + this.getTablePrefix() + "convert_stacked_item (entity_uuid, stack_size) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(itemInsert)) {
                    for (ConversionData data : itemData) {
                        statement.setString(1, data.getUniqueId().toString());
                        statement.setInt(2, data.getStackSize());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        });
    }

    public Map<StackType, Set<ConversionData>> getConversionData(Set<Entity> entities, Set<StackType> requiredStackTypes) {
        Map<StackType, Set<ConversionData>> conversionData = new HashMap<>();
        if (requiredStackTypes.isEmpty())
            return conversionData;

        this.databaseConnector.connect(connection -> {
            if (requiredStackTypes.contains(StackType.ENTITY))
                conversionData.put(StackType.ENTITY, this.getConversionData(entities, "entity", connection));

            if (requiredStackTypes.contains(StackType.ITEM))
                conversionData.put(StackType.ITEM, this.getConversionData(entities, "item", connection));
        });

        return conversionData;
    }

    private Set<ConversionData> getConversionData(Set<Entity> entities, String tableName, Connection connection) throws SQLException {
        Set<ConversionData> conversionData = new HashSet<>();

        Map<UUID, Entity> entityMap = new HashMap<>();
        for (Entity entity : entities)
            entityMap.put(entity.getUniqueId(), entity);

        if (entityMap.isEmpty())
            return conversionData;

        String entityUniqueIdsString = entityMap.keySet().stream().map(UUID::toString).map(x -> "'" + x + "'").collect(Collectors.joining(","));

        // Get data
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT entity_uuid, stack_size FROM " + this.getTablePrefix() + "convert_stacked_" + tableName + " WHERE entity_uuid IN (" + entityUniqueIdsString + ")";
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("entity_uuid"));
                Entity entity = entityMap.get(uuid);
                int stackSize = result.getInt("stack_size");
                conversionData.add(new ConversionData(entity, stackSize));
            }
        }

        // Delete data
        try (Statement statement = connection.createStatement()) {
            String query = "DELETE FROM " + this.getTablePrefix() + "convert_stacked_" + tableName + " WHERE entity_uuid IN (" + entityUniqueIdsString + ")";
            statement.executeUpdate(query);
        }

        return conversionData;
    }

    public List<String> getTranslationLocales(String requiredVersion) {
        List<String> locales = new ArrayList<>();

        this.databaseConnector.connect(connection -> {
            String delete = "DELETE FROM " + this.getTablePrefix() + "translation_locale WHERE version != ?";
            try (PreparedStatement statement = connection.prepareStatement(delete)) {
                statement.setString(1, requiredVersion);
                statement.executeUpdate();
            }

            String query = "SELECT name FROM " + this.getTablePrefix() + "translation_locale";
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(query);
                while (result.next())
                    locales.add(result.getString("name"));
            }
        });

        locales.sort(String::compareTo);

        return locales;
    }

    public void saveTranslationLocales(String version, List<String> locales) {
        this.databaseConnector.connect(connection -> {
            String insertQuery = "INSERT INTO " + this.getTablePrefix() + "translation_locale (version, name) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                for (String locale : locales) {
                    statement.setString(1, version);
                    statement.setString(2, locale);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
    }

    public static class StackCounts {
        private int entity, item, block, spawner;

        private StackCounts(int entity, int item, int block, int spawner) {
            this.entity = entity;
            this.item = item;
            this.block = block;
            this.spawner = spawner;
        }

        public int getEntityCount() {
            return this.entity;
        }

        public int getItemCount() {
            return this.item;
        }

        public int getBlockCount() {
            return this.block;
        }

        public int getSpawnerCount() {
            return this.spawner;
        }
    }

    private static class StackedBlockData {
        public int id;
        public int stackSize;
        public int chunkX, chunkZ;
        public int blockX, blockY, blockZ;
        public String world;

        public StackedBlockData(int id, int stackSize, int chunkX, int chunkZ, int blockX, int blockY, int blockZ, String world) {
            this.id = id;
            this.stackSize = stackSize;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
            this.world = world;
        }
    }

    private static class StackedSpawnerData extends StackedBlockData {
        public boolean placedByPlayer;

        public StackedSpawnerData(int id, int stackSize, int chunkX, int chunkZ, int blockX, int blockY, int blockZ, String world, boolean placedByPlayer) {
            super(id, stackSize, chunkX, chunkZ, blockX, blockY, blockZ, world);
            this.placedByPlayer = placedByPlayer;
        }
    }

    private static class StackedEntityData {
        public int id;
        public UUID entityUUID;
        public byte[] stackEntities;

        public StackedEntityData(int id, UUID entityUUID, byte[] stackEntities) {
            this.id = id;
            this.entityUUID = entityUUID;
            this.stackEntities = stackEntities;
        }
    }

    private static class StackedItemData {
        public int id;
        public int stackSize;
        public UUID entityUUID;

        public StackedItemData(int id, int stackSize, UUID entityUUID) {
            this.id = id;
            this.stackSize = stackSize;
            this.entityUUID = entityUUID;
        }
    }

}
