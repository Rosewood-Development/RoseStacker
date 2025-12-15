package dev.rosewood.rosestacker.nms.v1_21_R7.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.AbstractSimpleStackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.v1_21_R7.NMSHandlerImpl;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.LivingEntity;

public class SimpleStackedEntityDataStorage extends AbstractSimpleStackedEntityDataStorage {

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

        CompoundTag compoundTag = ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity);
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
