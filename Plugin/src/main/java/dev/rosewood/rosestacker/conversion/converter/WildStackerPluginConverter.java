package dev.rosewood.rosestacker.conversion.converter;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.database.DatabaseConnector;
import dev.rosewood.rosestacker.database.SQLiteConnector;
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;

public class WildStackerPluginConverter extends StackPluginConverter {

    private WildStackerPlugin wildStacker;

    public WildStackerPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "WildStacker", StackPlugin.WildStacker, ConverterType.ENTITY, ConverterType.ITEM);

        this.wildStacker = (WildStackerPlugin) this.plugin;
    }

    @Override
    public void convert() {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);
        StackManager stackManager = this.roseStacker.getManager(StackManager.class);

        // Force save loaded data
        SystemManager systemHandler = WildStackerAPI.getWildStacker().getSystemManager();
        systemHandler.performCacheSave();

        // Go through the database to be able to load all information
        DatabaseConnector connector = new SQLiteConnector(this.wildStacker, "database");
        connector.connect(connection -> {
            Map<StackType, Set<ConversionData>> conversionData = new HashMap<>();

            // Load entities
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT uuid, stackAmount FROM entities");
                Set<ConversionData> entityConversionData = new HashSet<>();
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));
                    int amount = result.getInt("stackAmount");
                    entityConversionData.add(new ConversionData(uuid, amount));
                }
                conversionData.put(StackType.ENTITY, entityConversionData);
            }

            // Load items
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT uuid, stackAmount FROM items");
                Set<ConversionData> itemConversionData = new HashSet<>();
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));
                    int amount = result.getInt("stackAmount");
                    itemConversionData.add(new ConversionData(uuid, amount));
                }
                conversionData.put(StackType.ITEM, itemConversionData);
            }

            dataManager.setConversionData(conversionData);

            // Load barrels (blocks)
            // Yes, this ends up loading chunks because I have no idea how WildStacker serializes the stack material
            try (Statement statement = connection.createStatement()) {
                Set<StackedBlock> stackedBlocks = new HashSet<>();

                ResultSet result = statement.executeQuery("SELECT location, stackAmount FROM barrels");
                while (result.next()) {
                    Location location = this.parseLocation(result.getString("location"), ',');
                    int amount = result.getInt("stackAmount");
                    Material type = systemHandler.getStackedSnapshot(location.getChunk()).getStackedBarrelItem(location).getValue().getType();
                    Block block = location.getBlock();

                    // Remove hologram thingy
                    StackedBarrel barrel = systemHandler.getStackedBarrel(block);
                    if (barrel != null)
                        barrel.removeDisplayBlock();

                    block.setType(type); // Set the block type to the stack type since we just removed the hologram thingy

                    // Stacks of 1 aren't really stacks
                    if (amount == 1)
                        continue;

                    stackedBlocks.add(new StackedBlock(amount, block));
                }

                dataManager.createOrUpdateStackedBlocksOrSpawners(stackedBlocks);
            }

            // Load spawners
            try (Statement statement = connection.createStatement()) {
                Set<StackedSpawner> stackedSpawners = new HashSet<>();

                ResultSet result = statement.executeQuery("SELECT location, stackAmount FROM spawners");
                while (result.next()) {
                    Location location = this.parseLocation(result.getString("location"), ',');
                    Block block = location.getBlock();
                    BlockState blockState = block.getState();
                    if (!(blockState instanceof CreatureSpawner))
                        continue;

                    int amount = result.getInt("stackAmount");

                    stackedSpawners.add(new StackedSpawner(amount, location));
                    stackManager.createSpawnerStack(block, amount);
                }

                dataManager.createOrUpdateStackedBlocksOrSpawners(stackedSpawners);
            }
        });
    }

}
