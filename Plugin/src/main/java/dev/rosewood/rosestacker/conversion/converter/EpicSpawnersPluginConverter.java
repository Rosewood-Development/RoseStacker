package dev.rosewood.rosestacker.conversion.converter;

import com.songoda.epicspawners.EpicSpawners;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import dev.rosewood.rosegarden.database.SQLiteConnector;
import dev.rosewood.rosestacker.conversion.StackPlugin;
import dev.rosewood.rosestacker.manager.DataManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class EpicSpawnersPluginConverter extends StackPluginConverter {

    private EpicSpawners epicSpawners;

    public EpicSpawnersPluginConverter(RosePlugin rosePlugin) {
        super(rosePlugin, "EpicSpawners", StackPlugin.EpicSpawners);

        this.epicSpawners = (EpicSpawners) this.plugin;
    }

    @Override
    public void convert() {
        // Query their database ourselves
        DatabaseConnector connector = new SQLiteConnector(this.epicSpawners);
        connector.connect(connection -> {
            Set<StackedSpawner> stackedSpawners = new HashSet<>();

            String tablePrefix = this.epicSpawners.getDataManager().getTablePrefix();
            try (Statement statement = connection.createStatement()) {
                String query = "SELECT amount, world, x, y, z FROM " + tablePrefix + "placed_spawners ps JOIN " + tablePrefix + "spawner_stacks ss ON ps.id = ss.spawner_id";
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));
                    if (world == null)
                        continue;

                    int amount = result.getInt("amount");
                    double x = result.getDouble("x");
                    double y = result.getDouble("y");
                    double z = result.getDouble("z");
                    Location location = new Location(world, x, y, z);

                    stackedSpawners.add(new StackedSpawner(amount, location));
                }
            }

            if (!stackedSpawners.isEmpty()) {
                Bukkit.getScheduler().runTask(this.rosePlugin, () -> {
                    StackManager stackManager = this.rosePlugin.getManager(StackManager.class);
                    for (StackedSpawner stackedSpawner : stackedSpawners)
                        stackManager.createSpawnerStack(stackedSpawner.getLocation().getBlock(), stackedSpawner.getStackSize(), false);
                });
            }
        });
    }

}
