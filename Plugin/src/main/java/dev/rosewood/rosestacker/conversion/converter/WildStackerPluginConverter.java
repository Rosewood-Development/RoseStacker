package dev.rosewood.rosestacker.conversion.converter;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import dev.rosewood.rosegarden.database.SQLiteConnector;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;

public class WildStackerPluginConverter extends StackPluginConverter {

    private WildStackerPlugin wildStacker;

    public WildStackerPluginConverter(RosePlugin rosePlugin) {
        super(rosePlugin, "WildStacker", StackPlugin.WildStacker, ConverterType.WS_ENTITY, ConverterType.WS_ITEM);

        this.wildStacker = (WildStackerPlugin) this.plugin;
    }

    @Override
    public void convert() {
        StackManager stackManager = this.rosePlugin.getManager(StackManager.class);

        // Force save loaded data
        SystemManager systemHandler = WildStackerAPI.getWildStacker().getSystemManager();
        systemHandler.performCacheSave();

        // Go through the database to be able to load all information
        DatabaseConnector connector = new SQLiteConnector(this.wildStacker, "database");
        connector.connect(connection -> {
            // Load barrels (blocks)
            Set<StackedBlock> stackedBlocks = new HashSet<>();
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT location, stackAmount FROM barrels");
                while (result.next()) {
                    Location location = this.parseLocation(result.getString("location"), ',');
                    if (location == null)
                        continue;

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
            }

            // Load spawners
            Set<StackedSpawner> stackedSpawners = new HashSet<>();
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT location, stackAmount FROM spawners");
                while (result.next()) {
                    Location location = this.parseLocation(result.getString("location"), ',');
                    if (location == null)
                        continue;

                    Block block = location.getBlock();
                    BlockState blockState = block.getState();
                    if (!(blockState instanceof CreatureSpawner))
                        continue;

                    int amount = result.getInt("stackAmount");

                    stackedSpawners.add(new StackedSpawner(amount, location));
                }
            }

            if (!stackedBlocks.isEmpty() || !stackedSpawners.isEmpty()) {
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
