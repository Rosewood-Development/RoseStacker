package dev.esophose.rosestacker.converter;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.handlers.SystemHandler;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.SQLiteConnector;
import dev.esophose.rosestacker.manager.ConversionManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;

public class WildStackerPluginConverter extends StackPluginConverter {

    private WildStackerPlugin wildStacker;

    public WildStackerPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "WildStacker");

        this.wildStacker = (WildStackerPlugin) this.plugin;
    }

    @Override
    public void convert() {
        ConversionManager conversionManager = this.roseStacker.getManager(ConversionManager.class);

        // Go through the database to be able to load all information
        DatabaseConnector connector = new SQLiteConnector(this.wildStacker, "database");
        connector.connect(connection -> {
            // Force save loaded data
            SystemManager systemManager = this.wildStacker.getSystemManager();
            systemManager.performCacheSave();

            // Load entities
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT uuid, stackAmount FROM entities");
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));
                    int amount = result.getInt("stackAmount");
                    // TODO
                }
            }

            // Load items
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT uuid, stackAmount FROM items");
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("uuid"));
                    int amount = result.getInt("stackAmount");
                    // TODO
                }
            }

            // Load barrels (blocks)
            // Yes, this ends up loading chunks because I have no idea how WildStacker serializes the stack material
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT location, stackAmount FROM barrels");
                while (result.next()) {
                    Location location = this.parseLocation(result.getString("location"), ',');
                    int amount = result.getInt("stackAmount");
                    Material type = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(location.getChunk()).getStackedBarrel(location).getValue();
                    // TODO
                }
            }

            // Load spawners
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT location, stackAmount FROM spawners");
                while (result.next()) {
                    Location location = this.parseLocation(result.getString("location"), ',');
                    int amount = result.getInt("stackAmount");
                    // TODO
                }
            }
        });
    }

}
