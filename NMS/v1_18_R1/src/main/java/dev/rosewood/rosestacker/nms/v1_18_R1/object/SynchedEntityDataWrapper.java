package dev.rosewood.rosestacker.nms.v1_18_R1.object;

import java.util.List;
import net.minecraft.network.syncher.SynchedEntityData;

public class SynchedEntityDataWrapper extends SynchedEntityData {

    private final List<DataItem<?>> dataItems;

    public SynchedEntityDataWrapper(List<DataItem<?>> dataItems) {
        super(null);
        this.dataItems = dataItems;
    }

    @Override
    public List<DataItem<?>> packDirty() {
        return this.dataItems;
    }

}
