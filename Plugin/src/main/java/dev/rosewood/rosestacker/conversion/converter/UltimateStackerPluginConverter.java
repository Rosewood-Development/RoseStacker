package dev.rosewood.rosestacker.conversion.converter;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.core.database.DatabaseConnector;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.manager.DataManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class UltimateStackerPluginConverter extends StackPluginConverter {

    private final UltimateStacker ultimateStacker;

    public UltimateStackerPluginConverter(RosePlugin rosePlugin) {
        super(rosePlugin, "UltimateStacker", StackPlugin.UltimateStacker, ConverterType.ENTITY, ConverterType.ULTIMATESTACKER);

        this.ultimateStacker = (UltimateStacker) this.plugin;
    }

    @Override
    public void convert() {
        // If EpicSpawners is installed, spawner stacking functionality is handled by that plugin instead
        if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners"))
            return;

        DataManager dataManager = this.rosePlugin.getManager(DataManager.class);

        // Force save loaded data
        this.ultimateStacker.getDataManager().bulkUpdateSpawners(this.ultimateStacker.getSpawnerStackManager().getStacks());

        // Go through the database to be able to load all spawner information
        DatabaseConnector connector = this.ultimateStacker.getDatabaseConnector();
        connector.connect(connection -> {
            // Load entities
            try (Statement statement = connection.createStatement()) {
                String query = "SELECT he.uuid AS uuid, COUNT(se.host) + 1 AS stackAmount FROM ultimatestacker_host_entities he " +
                        "JOIN ultimatestacker_stacked_entities se ON he.id = se.host " +
                        "GROUP BY se.host";
                ResultSet result = statement.executeQuery(query);

                Set<ConversionData> entityConversionData = new HashSet<>();
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));
                    int amount = result.getInt("stackAmount");
                    entityConversionData.add(new ConversionData(uuid, amount));
                }

                Map<StackType, Set<ConversionData>> conversionData = new HashMap<>();
                conversionData.put(StackType.ENTITY, entityConversionData);
                dataManager.setConversionData(conversionData);
            }

            // Load blocks
            Set<StackedBlock> stackedBlocks = new HashSet<>();
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT amount, world, x, y, z FROM ultimatestacker_blocks");
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));
                    if (world == null)
                        continue;

                    int amount = result.getInt("amount");
                    if (amount == 1)
                        continue;

                    Block block = world.getBlockAt(result.getInt("x"), result.getInt("y"), result.getInt("z"));
                    stackedBlocks.add(new StackedBlock(amount, block));
                }
            }

            // Load spawners
            Set<StackedSpawner> stackedSpawners = new HashSet<>();
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT amount, world, x, y, z FROM ultimatestacker_spawners");
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));
                    if (world == null)
                        continue;

                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");

                    Location location = new Location(world, x, y, z);
                    int amount = result.getInt("amount");
                    stackedSpawners.add(new StackedSpawner(amount, location));
                }
            }

            if (!stackedBlocks.isEmpty() || !stackedSpawners.isEmpty()) {
                StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
                Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                    for (StackedBlock stackedBlock : stackedBlocks)
                        stackManager.createBlockStack(stackedBlock.getLocation().getBlock(), stackedBlock.getStackSize());

                    for (StackedSpawner stackedSpawner : stackedSpawners)
                        stackManager.createSpawnerStack(stackedSpawner.getLocation().getBlock(), stackedSpawner.getStackSize(), false);
                });
            }
        });
    }

}
