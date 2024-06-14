package dev.rosewood.rosestacker.nms.v1_21_R1.storage;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.AbstractSimpleStackedEntityDataStorage;
import dev.rosewood.rosestacker.nms.v1_21_R1.NMSHandlerImpl;
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
        CompoundTag tag = new CompoundTag();
        LivingEntity entity = this.entity.get();
        if (entity == null)
            return new NBTEntityDataEntry(tag);

        ((NMSHandlerImpl) NMSAdapter.getHandler()).saveEntityToTag(entity, tag);
        this.stripUnneeded(tag);
        return new NBTEntityDataEntry(tag);
    }

    private void stripUnneeded(CompoundTag compoundTag) {
        NMSHandler.REMOVABLE_NBT_KEYS.forEach(compoundTag::remove);
        CompoundTag bukkitValues = compoundTag.getCompound("BukkitValues");
        bukkitValues.remove("rosestacker:stacked_entity_data");
        NMSHandler.UNSAFE_NBT_KEYS.forEach(compoundTag::remove);
    }

}
