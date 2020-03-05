package dev.esophose.rosestacker.conversion.converter;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.handlers.SystemHandler;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.ConversionData;
import dev.esophose.rosestacker.conversion.ConverterType;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.SQLiteConnector;
import dev.esophose.rosestacker.manager.DataManager;
import dev.esophose.rosestacker.stack.Stack;
import dev.esophose.rosestacker.stack.StackType;
import dev.esophose.rosestacker.stack.StackedBlock;
import dev.esophose.rosestacker.stack.StackedSpawner;
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

public class WildStackerPluginConverter extends StackPluginConverter {

    private WildStackerPlugin wildStacker;

    public WildStackerPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "WildStacker", ConverterType.ENTITY, ConverterType.ITEM);

        this.wildStacker = (WildStackerPlugin) this.plugin;
    }

    @Override
    public void convert() {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        // Force save loaded data
        SystemHandler systemHandler = this.wildStacker.getSystemManager();
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

            Set<Stack> stackedBlocksAndSpawners = new HashSet<>();

            // Load barrels (blocks)
            // Yes, this ends up loading chunks because I have no idea how WildStacker serializes the stack material
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT location, stackAmount FROM barrels");
                while (result.next()) {
                    Location location = this.parseLocation(result.getString("location"), ',');
                    int amount = result.getInt("stackAmount");
                    Material type = systemHandler.getStackedSnapshot(location.getChunk()).getStackedBarrel(location).getValue();
                    Block block = location.getBlock();

                    WStackedBarrel.of(block).removeDisplayBlock(); // Remove hologram thingy

                    if (amount == 1) {
                        block.setType(type);
                        continue;
                    }

                    stackedBlocksAndSpawners.add(new StackedBlock(amount, block));
                }
            }

            // Load spawners
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT location, stackAmount FROM spawners");
                while (result.next()) {
                    Location location = this.parseLocation(result.getString("location"), ',');
                    int amount = result.getInt("stackAmount");
                    stackedBlocksAndSpawners.add(new StackedSpawner(amount, location));
                }
            }

            dataManager.createOrUpdateStackedBlocksOrSpawners(stackedBlocksAndSpawners);
        });
    }

}
