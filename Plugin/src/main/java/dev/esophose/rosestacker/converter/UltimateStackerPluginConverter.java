package dev.esophose.rosestacker.converter;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.core.database.DatabaseConnector;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConversionManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class UltimateStackerPluginConverter extends StackPluginConverter {

    private UltimateStacker ultimateStacker;

    public UltimateStackerPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "UltimateStacker");

        this.ultimateStacker = (UltimateStacker) this.plugin;
    }

    @Override
    public void convert() {
        ConversionManager conversionManager = this.roseStacker.getManager(ConversionManager.class);

        // Go through the database to be able to load all spawner information
        DatabaseConnector connector = this.ultimateStacker.getDatabaseConnector();
        connector.connect(connection -> {
            // Force save loaded data
            this.ultimateStacker.getDataManager().bulkUpdateSpawners(this.ultimateStacker.getSpawnerStackManager().getStacks());

            // Load spawners
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
                    // TODO
                }
            }
        });
    }

}
