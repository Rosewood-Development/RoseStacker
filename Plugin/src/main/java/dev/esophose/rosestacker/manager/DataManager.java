package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.ConversionData;
import dev.esophose.rosestacker.conversion.ConverterType;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.MySQLConnector;
import dev.esophose.rosestacker.database.SQLiteConnector;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.Stack;
import dev.esophose.rosestacker.stack.StackType;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.StackedItem;
import dev.esophose.rosestacker.stack.StackedSpawner;
import dev.esophose.rosestacker.utils.EntitySerializer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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

public class DataManager extends Manager {

    private DatabaseConnector databaseConnector;
    private boolean ranVacuum;

    public DataManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.ranVacuum = false;
    }

    @Override
    public void reload() {
        this.disable();

        try {
            if (Setting.MYSQL_ENABLED.getBoolean()) {
                String hostname = Setting.MYSQL_HOSTNAME.getString();
                int port = Setting.MYSQL_PORT.getInt();
                String database = Setting.MYSQL_DATABASE_NAME.getString();
                String username = Setting.MYSQL_USER_NAME.getString();
                String password = Setting.MYSQL_USER_PASSWORD.getString();
                boolean useSSL = Setting.MYSQL_USE_SSL.getBoolean();

                this.databaseConnector = new MySQLConnector(this.roseStacker, hostname, port, database, username, password, useSSL);
                this.roseStacker.getLogger().info("Data handler connected using MySQL.");
            } else {
                this.databaseConnector = new SQLiteConnector(this.roseStacker);
                this.roseStacker.getLogger().info("Data handler connected using SQLite.");
            }
        } catch (Exception ex) {
            this.roseStacker.getLogger().severe("Fatal error trying to connect to database. Please make sure all your connection settings are correct and try again. Plugin has been disabled.");
            Bukkit.getPluginManager().disablePlugin(this.roseStacker);
        }

        // Vacuum the database to help compress it, only run once per plugin startup
        if (!this.ranVacuum && this.databaseConnector instanceof SQLiteConnector)
            this.databaseConnector.connect((connection) -> connection.createStatement().execute("VACUUM"));
    }

    @Override
    public void disable() {
        if (this.databaseConnector == null)
            return;

        // Wait for all database connections to finish
        long now = System.currentTimeMillis();
        long deadline = now + 5000; // Wait at most 5 seconds
        synchronized (this.databaseConnector.getLock()) {
            while (!this.databaseConnector.isFinished() && now < deadline) {
                try {
                    this.databaseConnector.getLock().wait(deadline - now);
                    now = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.databaseConnector.closeConnection();
    }

    public boolean isConnected() {
        return this.databaseConnector != null;
    }

    public void getStackedEntities(Set<Chunk> chunks, Consumer<Set<StackedEntity>> callback) {
        this.databaseConnector.connect(connection -> {
            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_entity WHERE world = '%s' AND chunk_x = %d AND chunk_z = %d";

            int count = 0;
            StringBuilder compoundSelect = new StringBuilder();
            Set<Entity> chunkEntities = new HashSet<>();
            Iterator<Chunk> chunkIterator = chunks.iterator();
            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                try {
                    Collections.addAll(chunkEntities, chunk.getEntities());
                } catch (Exception ignored) { }
                if (compoundSelect.length() > 0)
                    compoundSelect.append(" UNION ALL ");
                compoundSelect.append(String.format(select, chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));

                if (++count >= 500 || !chunkIterator.hasNext()) {
                    Set<StackedEntityData> stackedEntityData = new HashSet<>();

                    try (Statement statement = connection.createStatement()) {
                        ResultSet result = statement.executeQuery(compoundSelect.toString());
                        while (result.next()) {
                            stackedEntityData.add(new StackedEntityData(
                                    result.getInt("id"),
                                    UUID.fromString(result.getString("entity_uuid")),
                                    result.getBytes("stack_entities")
                            ));
                        }
                    }

                    Runnable task = () -> {
                        Set<StackedEntity> stackedEntities = new HashSet<>();
                        Set<Stack> cleanup = new HashSet<>();

                        for (StackedEntityData stackData : stackedEntityData) {
                            Optional<Entity> entity = chunkEntities.stream().filter(x -> x != null && x.getUniqueId().equals(stackData.entityUUID)).findFirst();
                            if (entity.isPresent()) {
                                stackedEntities.add(EntitySerializer.fromBlob(stackData.id, (LivingEntity) entity.get(), stackData.stackEntities));
                            } else {
                                cleanup.add(new StackedEntity(stackData.id, null, null, null));
                            }
                        }

                        callback.accept(stackedEntities);

                        if (!cleanup.isEmpty())
                            Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> this.deleteStacks(cleanup));
                    };

                    if (Bukkit.isPrimaryThread()) {
                        task.run();
                    } else {
                        Bukkit.getScheduler().runTask(this.roseStacker, task);
                    }

                    compoundSelect.setLength(0);
                    count = 0;
                }
            }
        });
    }

    public void getStackedItems(Set<Chunk> chunks, Consumer<Set<StackedItem>> callback) {
        this.databaseConnector.connect(connection -> {
            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_item WHERE world = '%s' AND chunk_x = %d AND chunk_z = %d";

            int count = 0;
            StringBuilder compoundSelect = new StringBuilder();
            Set<Entity> chunkEntities = new HashSet<>();
            Iterator<Chunk> chunkIterator = chunks.iterator();
            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                try {
                    Collections.addAll(chunkEntities, chunk.getEntities());
                } catch (Exception ignored) { }
                if (compoundSelect.length() > 0)
                    compoundSelect.append(" UNION ALL ");
                compoundSelect.append(String.format(select, chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));

                if (++count >= 500 || !chunkIterator.hasNext()) {
                    Set<StackedItemData> stackedItemData = new HashSet<>();

                    try (Statement statement = connection.createStatement()) {
                        ResultSet result = statement.executeQuery(compoundSelect.toString());
                        while (result.next()) {
                            stackedItemData.add(new StackedItemData(
                                    result.getInt("id"),
                                    result.getInt("stack_size"),
                                    UUID.fromString(result.getString("entity_uuid"))
                            ));
                        }
                    }

                    Runnable task = () -> {
                        Set<StackedItem> stackedItems = new HashSet<>();
                        Set<Stack> cleanup = new HashSet<>();

                        for (StackedItemData stackData : stackedItemData) {
                            Optional<Entity> entity = chunkEntities.stream().filter(x -> x != null && x.getUniqueId().equals(stackData.entityUUID)).findFirst();
                            if (entity.isPresent()) {
                                stackedItems.add(new StackedItem(stackData.id, stackData.stackSize, (Item) entity.get()));
                            } else {
                                cleanup.add(new StackedItem(stackData.id, 0, null));
                            }
                        }

                        callback.accept(stackedItems);

                        if (!cleanup.isEmpty())
                            Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> this.deleteStacks(cleanup));
                    };

                    if (Bukkit.isPrimaryThread()) {
                        task.run();
                    } else {
                        Bukkit.getScheduler().runTask(this.roseStacker, task);
                    }

                    compoundSelect.setLength(0);
                    count = 0;
                }
            }
        });
    }

    public void getStackedBlocks(Set<Chunk> chunks, Consumer<Set<StackedBlock>> callback) {
        this.databaseConnector.connect(connection -> {
            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_block WHERE world = '%s' AND chunk_x = %d AND chunk_z = %d";

            int count = 0;
            StringBuilder compoundSelect = new StringBuilder();
            Iterator<Chunk> chunkIterator = chunks.iterator();
            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                if (compoundSelect.length() > 0)
                    compoundSelect.append(" UNION ALL ");
                compoundSelect.append(String.format(select, chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));

                if (++count >= 500 || !chunkIterator.hasNext()) {
                    Set<StackedBlockData> stackedBlockData = new HashSet<>();

                    try (Statement statement = connection.createStatement()) {
                        ResultSet result = statement.executeQuery(compoundSelect.toString());
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

                    Runnable task = () -> {
                        Set<StackedBlock> stackedBlocks = new HashSet<>();
                        Set<Stack> cleanup = new HashSet<>();

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
                            Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> this.deleteStacks(cleanup));
                    };

                    if (Bukkit.isPrimaryThread()) {
                        task.run();
                    } else {
                        Bukkit.getScheduler().runTask(this.roseStacker, task);
                    }

                    compoundSelect.setLength(0);
                    count = 0;
                }
            }
        });
    }

    public void getStackedSpawners(Set<Chunk> chunks, Consumer<Set<StackedSpawner>> callback) {
        this.databaseConnector.connect(connection -> {
            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_spawner WHERE world = '%s' AND chunk_x = %d AND chunk_z = %d";

            int count = 0;
            StringBuilder compoundSelect = new StringBuilder();
            Iterator<Chunk> chunkIterator = chunks.iterator();
            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                if (compoundSelect.length() > 0)
                    compoundSelect.append(" UNION ALL ");
                compoundSelect.append(String.format(select, chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));

                if (++count >= 500 || !chunkIterator.hasNext()) {
                    Set<StackedBlockData> stackedSpawnerData = new HashSet<>();

                    try (Statement statement = connection.createStatement()) {
                        ResultSet result = statement.executeQuery(compoundSelect.toString());
                        while (result.next()) {
                            stackedSpawnerData.add(new StackedBlockData(
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

                    Runnable task = () -> {
                        Set<StackedSpawner> stackedSpawners = new HashSet<>();
                        Set<Stack> cleanup = new HashSet<>();

                        for (StackedBlockData stackData : stackedSpawnerData) {
                            World world = Bukkit.getWorld(stackData.world);
                            Block block = null;

                            boolean invalid = world == null;
                            if (!invalid) {
                                block = world.getBlockAt((stackData.chunkX << 4) + stackData.blockX, stackData.blockY, (stackData.chunkZ << 4) + stackData.blockZ);
                                if (block.getType() != Material.SPAWNER)
                                    invalid = true;
                            }

                            if (!invalid) {
                                stackedSpawners.add(new StackedSpawner(stackData.id, stackData.stackSize, (CreatureSpawner) block.getState()));
                            } else {
                                cleanup.add(new StackedBlock(stackData.id, 0, null));
                            }
                        }

                        callback.accept(stackedSpawners);

                        if (!cleanup.isEmpty())
                            Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, () -> this.deleteStacks(cleanup));
                    };

                    if (Bukkit.isPrimaryThread()) {
                        task.run();
                    } else {
                        Bukkit.getScheduler().runTask(this.roseStacker, task);
                    }

                    compoundSelect.setLength(0);
                    count = 0;
                }
            }
        });
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
                        statement.setInt(4, stack.getLocation().getChunk().getX());
                        statement.setInt(5, stack.getLocation().getChunk().getZ());
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
                        statement.setInt(4, stack.getLocation().getChunk().getX());
                        statement.setInt(5, stack.getLocation().getChunk().getZ());
                        // On conflict
                        statement.setBytes(6, EntitySerializer.toBlob(stack));
                        statement.setString(7, stack.getLocation().getWorld().getName());
                        statement.setInt(8, stack.getLocation().getChunk().getX());
                        statement.setInt(9, stack.getLocation().getChunk().getZ());
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
                        statement.setInt(4, stack.getLocation().getChunk().getX());
                        statement.setInt(5, stack.getLocation().getChunk().getZ());
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
                        statement.setInt(4, stack.getLocation().getChunk().getX());
                        statement.setInt(5, stack.getLocation().getChunk().getZ());
                        // On conflict
                        statement.setInt(6, stack.getStackSize());
                        statement.setString(7, stack.getLocation().getWorld().getName());
                        statement.setInt(8, stack.getLocation().getChunk().getX());
                        statement.setInt(9, stack.getLocation().getChunk().getZ());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public <T extends Stack> void createOrUpdateStackedBlocksOrSpawners(Collection<T> stacks) {
        if (stacks.isEmpty())
            return;

        String tableName = stacks.iterator().next() instanceof StackedBlock ? "stacked_block" : "stacked_spawner";
        this.databaseConnector.connect(connection -> {
            Set<Stack> update = stacks.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());
            Set<Stack> insert = stacks.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());

            if (!update.isEmpty()) {
                String batchUpdate = "UPDATE " + this.getTablePrefix() + tableName + " SET stack_size = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(batchUpdate)) {
                    for (Stack stack : update) {
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
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + tableName + " (stack_size, world, chunk_x, chunk_z, block_x, block_y, block_z) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (Stack stack : insert) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setString(2, stack.getLocation().getWorld().getName());
                        statement.setInt(3, stack.getLocation().getChunk().getX());
                        statement.setInt(4, stack.getLocation().getChunk().getZ());
                        statement.setInt(5, stack.getLocation().getBlockX() & 0xF);
                        statement.setInt(6, stack.getLocation().getBlockY());
                        statement.setInt(7, stack.getLocation().getBlockZ() & 0xF);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void deleteStacks(Set<Stack> stacks) {
        if (stacks.isEmpty())
            return;

        this.databaseConnector.connect(connection -> {
            Set<StackedBlock> stackedBlocks = new HashSet<>();
            Set<StackedEntity> stackedEntities = new HashSet<>();
            Set<StackedItem> stackedItems = new HashSet<>();
            Set<StackedSpawner> stackedSpawners = new HashSet<>();

            for (Stack stack : stacks) {
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

    private <T extends Stack> void deleteStackBatch(Connection connection, Set<T> stacks, String tableName) {
        String batchDelete = "DELETE FROM " + this.getTablePrefix() + tableName + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(batchDelete)) {
            for (Stack stack : stacks) {
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
        return new StackCounts(
                this.queryData(world, StackType.ENTITY),
                this.queryData(world, StackType.ITEM),
                this.queryData(world, StackType.BLOCK),
                this.queryData(world, StackType.SPAWNER)
        );
    }

    public int queryData(String world, StackType stackType) {
        AtomicInteger total = new AtomicInteger();
        this.databaseConnector.connect(connection -> {
            String query = "SELECT COUNT(*) FROM " + this.getTablePrefix() + "stacked_" + stackType.name().toLowerCase() + " WHERE world = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, world);
                ResultSet result = statement.executeQuery();
                result.next();
                total.addAndGet(result.getInt(1));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
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

            String query = insertIgnore + this.getTablePrefix() + " convert_handler (name) VALUES (?)";
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
                while (result.next())
                    conversionHandlers.add(ConverterType.valueOf(result.getString(1)));
            }
        });
        return conversionHandlers;
    }

    public void setConversionData(Map<StackType, Set<ConversionData>> conversionData) {
        this.databaseConnector.connect(connection -> {
            Set<ConversionData> entityData = conversionData.get(StackType.ENTITY);
            if (!entityData.isEmpty()) {
                String entityInsert = "INSERT INTO " + this.getTablePrefix() + "convert_entity (entity_uuid, stack_size) VALUES (?, ?)";
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
            if (!itemData.isEmpty()) {
                String itemInsert = "INSERT INTO " + this.getTablePrefix() + "convert_item (entity_uuid, stack_size) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(itemInsert)) {
                    for (ConversionData data : entityData) {
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

        if (requiredStackTypes.contains(StackType.ENTITY))
            conversionData.put(StackType.ENTITY, this.getConversionData(entities, "entity"));

        if (requiredStackTypes.contains(StackType.ITEM))
            conversionData.put(StackType.ITEM, this.getConversionData(entities, "item"));

        return conversionData;
    }

    private Set<ConversionData> getConversionData(Set<Entity> entities, String tableName) {
        Set<ConversionData> conversionData = new HashSet<>();

        Map<UUID, Entity> entityMap = new HashMap<>();
        for (Entity entity : entities)
            entityMap.put(entity.getUniqueId(), entity);

        if (entityMap.isEmpty())
            return conversionData;

        this.databaseConnector.connect(connection -> {
            // Get data
            Set<UUID> foundUniqueIds = new HashSet<>();
            try (Statement statement = connection.createStatement()) {
                String entityUniqueIdsString = entityMap.keySet().stream().map(UUID::toString).collect(Collectors.joining(","));
                String query = "SELECT entity_uuid, stack_size FROM " + this.getTablePrefix() + "convert_" + tableName + " WHERE entity_uuid IN (" + entityUniqueIdsString + ")";
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("entity_uuid"));
                    Entity entity = entityMap.get(uuid);
                    int stackSize = result.getInt("stack_size");
                    conversionData.add(new ConversionData(entity, stackSize));
                    foundUniqueIds.add(uuid);
                }
            }

            // Delete data
            try (Statement statement = connection.createStatement()) {
                String entityUniqueIdsString = foundUniqueIds.stream().map(UUID::toString).collect(Collectors.joining(","));
                String query = "DELETE FROM " + this.getTablePrefix() + "convert_" + tableName + " WHERE entity_uuid IN (" + entityUniqueIdsString + ")";
                statement.executeUpdate(query);
            }
        });

        return conversionData;
    }

    /**
     * @return The connector to the database
     */
    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    /**
     * @return the prefix to be used by all table names
     */
    public String getTablePrefix() {
        return this.roseStacker.getDescription().getName().toLowerCase() + '_';
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
        private int id;
        private int stackSize;
        private int chunkX, chunkZ;
        private int blockX, blockY, blockZ;
        private String world;

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

    private static class StackedEntityData {
        private int id;
        private UUID entityUUID;
        private byte[] stackEntities;

        public StackedEntityData(int id, UUID entityUUID, byte[] stackEntities) {
            this.id = id;
            this.entityUUID = entityUUID;
            this.stackEntities = stackEntities;
        }
    }

    private static class StackedItemData {
        private int id;
        private int stackSize;
        private UUID entityUUID;

        public StackedItemData(int id, int stackSize, UUID entityUUID) {
            this.id = id;
            this.stackSize = stackSize;
            this.entityUUID = entityUUID;
        }
    }

}
