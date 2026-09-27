package dev.rosewood.rosestacker.nms.v26_1_R1.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.AbstractSimpleStackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.v26_1_R1.NMSHandlerImpl;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.LivingEntity;

public class SimpleStackedEntityDataStorage extends AbstractSimpleStackedEntityDataStorage {

    /**
     * The top-level keys {@link #stripUnneeded(CompoundTag)} removes. They make up most of the save cost (attributes,
     * position and motion, brain, equipment), so the save skips encoding them instead of discarding them afterwards.
     */
    private static final Set<String> SKIPPED_SAVE_KEYS;
    static {
        Set<String> keys = new HashSet<>(NMSHandler.REMOVABLE_NBT_KEYS);
        keys.addAll(NMSHandler.UNSAFE_NBT_KEYS);
        SKIPPED_SAVE_KEYS = Set.copyOf(keys);
    }

    public SimpleStackedEntityDataStorage(LivingEntity livingEntity) {
        super(livingEntity);
    }

    public SimpleStackedEntityDataStorage(LivingEntity livingEntity, byte[] data) {
        super(livingEntity, data);
    }

    @Override
    protected NBTEntityDataEntry copy() {
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return new NBTEntityDataEntry(new CompoundTag());

        CompoundTag compoundTag = ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity, SKIPPED_SAVE_KEYS);
        // Still required: the save cannot skip fields written without a name (MapCodec) or the nested stack data
        this.stripUnneeded(compoundTag);
        return new NBTEntityDataEntry(compoundTag);
    }

    private void stripUnneeded(CompoundTag compoundTag) {
        NMSHandler.REMOVABLE_NBT_KEYS.forEach(compoundTag::remove);
        CompoundTag bukkitValues = compoundTag.getCompoundOrEmpty("BukkitValues");
        bukkitValues.remove("rosestacker:stacked_entity_data");
        NMSHandler.UNSAFE_NBT_KEYS.forEach(compoundTag::remove);
    }

}
