package dev.esophose.rosestacker.converter;

import com.songoda.epicspawners.EpicSpawners;
import dev.esophose.rosestacker.RoseStacker;

public class EpicSpawnersPluginConverter extends StackPluginConverter {

    private EpicSpawners epicSpawners;

    public EpicSpawnersPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "EpicSpawners");

        this.epicSpawners = (EpicSpawners) this.plugin;
    }

    @Override
    public void convert() {

    }

}
