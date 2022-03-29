package dev.rosewood.rosestacker.nms.v1_16_R2.entity;

import java.util.List;
import net.minecraft.server.v1_16_R2.DataWatcher;

public class DataWatcherWrapper extends DataWatcher {

    private final List<DataWatcher.Item<?>> dataItems;

    public DataWatcherWrapper(List<DataWatcher.Item<?>> dataItems) {
        super(null);
        this.dataItems = dataItems;
    }

    @Override
    public List<DataWatcher.Item<?>> b() {
        return this.dataItems;
    }

}
