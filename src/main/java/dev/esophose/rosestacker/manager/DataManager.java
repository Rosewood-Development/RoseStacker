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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
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

    public void getStackedBlocks(Chunk chunk, boolean async, Consumer<Set<StackedBlock>> callback) {
        Set<StackedBlock> stackedBlocks = new HashSet<>();

        ConnectionCallback query = connection -> {
            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_block WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) {
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

            this.sync(() -> callback.accept(stackedBlocks));
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public Set<StackedBlock> getStackedBlocks(Set<World> worlds) {
        Set<StackedBlock> stackedBlocks = new HashSet<>();

        for (World world : worlds)
            for (Chunk chunk : world.getLoadedChunks())
                this.getStackedBlocks(chunk, false, stackedBlocks::addAll);

        return stackedBlocks;
    }

    public void getStackedEntities(Chunk chunk, boolean async, Consumer<Set<StackedEntity>> callback) {
        Set<StackedEntity> stackedEntities = new HashSet<>();

        ConnectionCallback query = connection -> {
            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_entity WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) {
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

            this.sync(() -> callback.accept(stackedEntities));
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public Set<StackedEntity> getStackedEntities(Set<World> worlds) {
        Set<StackedEntity> stackedEntities = new HashSet<>();

        for (World world : worlds)
            for (Chunk chunk : world.getLoadedChunks())
                this.getStackedEntities(chunk, false, stackedEntities::addAll);

        return stackedEntities;
    }

    public void getStackedItems(Chunk chunk, boolean async, Consumer<Set<StackedItem>> callback) {
        Set<StackedItem> stackedItems = new HashSet<>();

        ConnectionCallback query = connection -> {
            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_item WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) {
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

            this.sync(() -> callback.accept(stackedItems));
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public Set<StackedItem> getStackedItems(Set<World> worlds) {
        Set<StackedItem> stackedItems = new HashSet<>();

        for (World world : worlds)
            for (Chunk chunk : world.getLoadedChunks())
                this.getStackedItems(chunk, false, stackedItems::addAll);

        return stackedItems;
    }

    public void getStackedSpawners(Chunk chunk, boolean async, Consumer<Set<StackedSpawner>> callback) {
        Set<StackedSpawner> stackedSpawners = new HashSet<>();

        ConnectionCallback query = connection -> {
            String select = "SELECT * FROM " + this.getTablePrefix() + "stacked_spawner WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) {
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
                    if (block.getType() == Material.SPAWNER)
                        stackedSpawners.add(new StackedSpawner(id, stackSize, (CreatureSpawner) block.getState()));
                }
            }

            this.sync(() -> callback.accept(stackedSpawners));
        };

        if (async) {
            this.async(() -> this.databaseConnector.connect(query));
        } else {
            this.databaseConnector.connect(query);
        }
    }

    public Set<StackedSpawner> getStackedSpawners(Set<World> worlds) {
        Set<StackedSpawner> stackedSpawners = new HashSet<>();

        for (World world : worlds)
            for (Chunk chunk : world.getLoadedChunks())
                this.getStackedSpawners(chunk, false, stackedSpawners::addAll);

        return stackedSpawners;
    }

    public void createOrUpdateStackedBlocksOrSpawners(Set<Stack> stacks) {
        if (stacks.isEmpty())
            return;

        String tableName = stacks.iterator().next() instanceof StackedBlock ? "stacked_block" : "stacked_spawner";

        this.async(() -> this.databaseConnector.connect(connection -> {
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
        }));
    }

    public void createOrUpdateStackedEntities(Set<StackedEntity> stackedEntities) {
        if (stackedEntities.isEmpty())
            return;

        this.async(() -> this.databaseConnector.connect(connection -> {
            Set<StackedEntity> insert = stackedEntities.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());
            Set<StackedEntity> update = stackedEntities.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + "stacked_entity (entity_uuid, stack_entities, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (StackedEntity stack : update) {
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
        }));
    }

    public void createOrUpdateStackedItems(Set<StackedItem> stackedItems) {
        if (stackedItems.isEmpty())
            return;

        this.async(() -> this.databaseConnector.connect(connection -> {
            Set<StackedItem> insert = stackedItems.stream().filter(x -> x.getId() == -1).collect(Collectors.toSet());
            Set<StackedItem> update = stackedItems.stream().filter(x -> x.getId() != -1).collect(Collectors.toSet());

            if (!insert.isEmpty()) {
                String batchInsert = "INSERT INTO " + this.getTablePrefix() + "stacked_item (stack_size, entity_uuid, world, chunk_x, chunk_z) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(batchInsert)) {
                    for (StackedItem stack : update) {
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
        }));
    }

    public void deleteStacks(Set<Stack> stacks) {
        if (stacks.isEmpty())
            return;

        String tableName;
        Stack first = stacks.iterator().next();
        if (first instanceof StackedBlock) {
            tableName = "stacked_block";
        } else if (first instanceof StackedEntity) {
            tableName = "stacked_entity";
        } else if (first instanceof StackedItem) {
            tableName = "stacked_item";
        } else if (first instanceof StackedSpawner) {
            tableName = "stacked_spawner";
        } else {
            return;
        }

        this.async(() -> this.databaseConnector.connect(connection -> {
            String batchDelete = "DELETE FROM " + this.getTablePrefix() + tableName + " WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(batchDelete)) {
                for (Stack stack : stacks) {
                    statement.setInt(1, stack.getId());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }));
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
