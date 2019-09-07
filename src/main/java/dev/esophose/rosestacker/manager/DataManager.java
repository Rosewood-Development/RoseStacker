package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.DatabaseConnector.ConnectionCallback;
import dev.esophose.rosestacker.database.MySQLConnector;
import dev.esophose.rosestacker.database.SQLiteConnector;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import dev.esophose.rosestacker.stack.Stack;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.StackedEntity;
import dev.esophose.rosestacker.stack.StackedItem;
import dev.esophose.rosestacker.stack.StackedSpawner;
import dev.esophose.rosestacker.utils.EntitySerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataManager extends Manager {

    private DatabaseConnector databaseConnector;

    public DataManager(RoseStacker roseStacker) {
        super(roseStacker);
    }

    @Override
    public void reload() {
        if (this.databaseConnector != null)
            this.databaseConnector.closeConnection();

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
    }

    @Override
    public void disable() {
        this.databaseConnector.closeConnection();
    }

    public void getStackedBlocks(Set<Chunk> chunks, boolean async, Consumer<Set<StackedBlock>> callback) {
        ConnectionCallback query = connection -> {
            Set<StackedBlock> stackedBlocks = new HashSet<>();

            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_block WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) {
                for (Chunk chunk : chunks) {
                    statement.setString(1, chunk.getWorld().getName());
                    statement.setInt(2, chunk.getX());
                    statement.setInt(3, chunk.getZ());

                    ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        int id = result.getInt("id");
                        int stackSize = result.getInt("stack_size");
                        int blockX = result.getInt("block_x");
                        int blockY = result.getInt("block_y");
                        int blockZ = result.getInt("block_z");
                        Block block = chunk.getBlock(blockX, blockY, blockZ);
                        stackedBlocks.add(new StackedBlock(id, stackSize, block));
                    }
                }
            }

            this.sync(() -> {
                stackedBlocks.forEach(StackedBlock::updateDisplay);
                callback.accept(stackedBlocks);
            });
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public void getStackedEntities(Set<Chunk> chunks, boolean async, Consumer<Set<StackedEntity>> callback) {
        ConnectionCallback query = connection -> {
            Set<StackedEntity> stackedEntities = new HashSet<>();

            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_entity WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) {
                for (Chunk chunk : chunks) {
                    statement.setString(1, chunk.getWorld().getName());
                    statement.setInt(2, chunk.getX());
                    statement.setInt(3, chunk.getZ());

                    ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        int id = result.getInt("id");
                        UUID entityUUID = UUID.fromString(result.getString("entity_uuid"));
                        List<String> stackEntities = EntitySerializer.fromBase64(result.getString("stack_entities"));
                        Optional<Entity> entity = Stream.of(chunk.getEntities()).filter(x -> x.getUniqueId().equals(entityUUID)).findFirst();
                        entity.ifPresent(x -> stackedEntities.add(new StackedEntity(id, (LivingEntity) x, stackEntities)));
                    }
                }
            }

            this.sync(() -> {
                stackedEntities.forEach(StackedEntity::updateDisplay);
                callback.accept(stackedEntities);
            });
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public void getStackedItems(Set<Chunk> chunks, boolean async, Consumer<Set<StackedItem>> callback) {
        ConnectionCallback query = connection -> {
            Set<StackedItem> stackedItems = new HashSet<>();

            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_item WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) {
                for (Chunk chunk : chunks) {
                    statement.setString(1, chunk.getWorld().getName());
                    statement.setInt(2, chunk.getX());
                    statement.setInt(3, chunk.getZ());

                    ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        int id = result.getInt("id");
                        int stackSize = result.getInt("stack_size");
                        UUID entityUUID = UUID.fromString(result.getString("entity_uuid"));
                        Optional<Entity> entity = Stream.of(chunk.getEntities()).filter(x -> x.getUniqueId().equals(entityUUID)).findFirst();
                        entity.ifPresent(x -> stackedItems.add(new StackedItem(id, stackSize, (Item) x)));
                    }
                }
            }

            this.sync(() -> {
                stackedItems.forEach(StackedItem::updateDisplay);
                callback.accept(stackedItems);
            });
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public void getStackedSpawners(Set<Chunk> chunks, boolean async, Consumer<Set<StackedSpawner>> callback) {
        ConnectionCallback query = connection -> {
            Set<StackedSpawner> stackedSpawners = new HashSet<>();

            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_spawner WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) {
                for (Chunk chunk : chunks) {
                    statement.setString(1, chunk.getWorld().getName());
                    statement.setInt(2, chunk.getX());
                    statement.setInt(3, chunk.getZ());

                    ResultSet result = statement.executeQuery();
                    this.sync(() -> { // Getting a block state must be done synchronously
                        try {
                            while (result.next()) {
                                int id = result.getInt("id");
                                int stackSize = result.getInt("stack_size");
                                int blockX = result.getInt("block_x");
                                int blockY = result.getInt("block_y");
                                int blockZ = result.getInt("block_z");
                                Block block = chunk.getBlock(blockX, blockY, blockZ);
                                if (block.getType() == Material.SPAWNER)
                                    stackedSpawners.add(new StackedSpawner(id, stackSize, (CreatureSpawner) block.getState()));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        stackedSpawners.forEach(StackedSpawner::updateDisplay);
                        callback.accept(stackedSpawners);
                    });
                }
            }
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public <T extends Stack> void createOrUpdateStackedBlocksOrSpawners(Set<T> stacks, boolean async) {
        if (stacks.isEmpty())
            return;

        String tableName = stacks.iterator().next() instanceof StackedBlock ? "stacked_block" : "stacked_spawner";

        ConnectionCallback query = connection -> {
            Set<Stack> insert = stacks.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());
            Set<Stack> update = stacks.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + tableName + " (stack_size, world, chunk_x, chunk_z, block_x, block_y, block_z) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (Stack stack : stacks) {
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
                }
            }

            if (!update.isEmpty()) {
                String batchUpdate = "UPDATE " + this.getTablePrefix() + tableName + " SET stack_size = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(batchUpdate)) {
                    for (Stack stack : update) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setInt(2, stack.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public void createOrUpdateStackedEntities(Set<StackedEntity> stackedEntities, boolean async) {
        if (stackedEntities.isEmpty())
            return;

        ConnectionCallback query = connection -> {
            Set<StackedEntity> insert = stackedEntities.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());
            Set<StackedEntity> update = stackedEntities.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + "stacked_entity (entity_uuid, stack_entities, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (StackedEntity stack : insert) {
                        statement.setString(1, stack.getEntity().getUniqueId().toString());
                        statement.setString(2, EntitySerializer.toBase64(stack.getStackedEntityNBTStrings()));
                        statement.setString(3, stack.getLocation().getWorld().getName());
                        statement.setInt(4, stack.getLocation().getChunk().getX());
                        statement.setInt(5, stack.getLocation().getChunk().getZ());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }

            if (!update.isEmpty()) {
                String batchUpdate = "UPDATE " + this.getTablePrefix() + "stacked_entity SET entity_uuid = ?, stack_entities = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(batchUpdate)) {
                    for (StackedEntity stack : update) {
                        statement.setString(1, stack.getEntity().getUniqueId().toString());
                        statement.setString(2, EntitySerializer.toBase64(stack.getStackedEntityNBTStrings()));
                        statement.setInt(3, stack.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public void createOrUpdateStackedItems(Set<StackedItem> stackedItems, boolean async) {
        if (stackedItems.isEmpty())
            return;

        ConnectionCallback query = connection -> {
            Set<StackedItem> insert = stackedItems.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());
            Set<StackedItem> update = stackedItems.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + "stacked_item (stack_size, entity_uuid, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (StackedItem stack : insert) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setString(2, stack.getItem().getUniqueId().toString());
                        statement.setString(3, stack.getLocation().getWorld().getName());
                        statement.setInt(4, stack.getLocation().getChunk().getX());
                        statement.setInt(5, stack.getLocation().getChunk().getZ());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }

            if (!update.isEmpty()) {
                String batchUpdate = "UPDATE " + this.getTablePrefix() + "stacked_item SET stack_size = ?, entity_uuid = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(batchUpdate)) {
                    for (StackedItem stack : update) {
                        statement.setInt(1, stack.getStackSize());
                        statement.setString(2, stack.getItem().getUniqueId().toString());
                        statement.setInt(3, stack.getId());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public void deleteStacks(Set<Stack> stacks) {
        if (stacks.isEmpty())
            return;

        this.async(() -> this.databaseConnector.connect(connection -> {
            Set<StackedBlock> stackedBlocks = new HashSet<>();
            Set<StackedEntity> stackedEntities = new HashSet<>();
            Set<StackedItem> stackedItems = new HashSet<>();
            Set<StackedSpawner> stackedSpawners = new HashSet<>();

            for (Stack stack : stacks) {
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
        }));
    }

    private <T extends Stack> void deleteStackBatch(Connection connection, Set<T> stacks, String tableName) throws SQLException {
        String batchDelete = "DELETE FROM " + this.getTablePrefix() + tableName + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(batchDelete)) {
            for (Stack stack : stacks) {
                statement.setInt(1, stack.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
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

    public void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this.roseStacker, runnable);
    }

    public void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.roseStacker, runnable);
    }

}
