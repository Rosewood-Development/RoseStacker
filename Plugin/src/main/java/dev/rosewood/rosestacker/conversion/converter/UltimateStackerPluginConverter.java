package dev.rosewood.rosestacker.conversion.converter;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.core.database.DatabaseConnector;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConverterType;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.manager.DataManager;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class UltimateStackerPluginConverter extends StackPluginConverter {

    private UltimateStacker ultimateStacker;

    public UltimateStackerPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "UltimateStacker", StackPlugin.UltimateStacker, ConverterType.ULTIMATESTACKER_ENTITY, ConverterType.ULTIMATESTACKER_ITEM);

        this.ultimateStacker = (UltimateStacker) this.plugin;
    }

    @Override
    public void convert() {
        // If EpicSpawners is installed, spawner stacking functionality is handled by that plugin instead
        if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners"))
            return;

        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        // Force save loaded data
        this.ultimateStacker.getDataManager().bulkUpdateSpawners(this.ultimateStacker.getSpawnerStackManager().getStacks());

        // Go through the database to be able to load all spawner information
        DatabaseConnector connector = this.ultimateStacker.getDatabaseConnector();
        connector.connect(connection -> {
            // Load spawners
            try (Statement statement = connection.createStatement()) {
                Set<StackedSpawner> stackedSpawners = new HashSet<>();

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

                dataManager.createOrUpdateStackedBlocksOrSpawners(stackedSpawners);
            }
        });
    }

}
