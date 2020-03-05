package dev.esophose.rosestacker.conversion.converter;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.storage.Storage;
import com.songoda.epicspawners.storage.StorageRow;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.DataManager;
import dev.esophose.rosestacker.stack.StackedSpawner;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;

public class EpicSpawnersPluginConverter extends StackPluginConverter {

    private EpicSpawners epicSpawners;

    public EpicSpawnersPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "EpicSpawners");

        this.epicSpawners = (EpicSpawners) this.plugin;
    }

    @Override
    public void convert() throws Exception {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        // Go through the storage system to be able to load all spawner information
        // The storage field is private and there's no getter, so... just get it anyway
        Field storageField = EpicSpawners.class.getDeclaredField("storage");
        storageField.setAccessible(true);
        Storage storage = (Storage) storageField.get(this.epicSpawners);

        // Force save loaded data
        storage.doSave();

        Set<StackedSpawner> stackedSpawners = new HashSet<>();
        for (StorageRow row : storage.getRowsByGroup("spawners")) {
            try {
                Location location = this.parseLocation(row.get("location").asString(), ':');
                int amount = Integer.parseInt(row.get("stacks").asString().split(":")[1]);
                stackedSpawners.add(new StackedSpawner(amount, location));
            } catch (Exception ignored) { }
        }
        dataManager.createOrUpdateStackedBlocksOrSpawners(stackedSpawners);
    }

}
