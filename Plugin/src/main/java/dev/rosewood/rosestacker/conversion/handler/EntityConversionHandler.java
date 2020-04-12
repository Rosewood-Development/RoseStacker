package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedEntity;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.LivingEntity;

public class EntityConversionHandler extends ConversionHandler {

    public EntityConversionHandler(RoseStacker roseStacker) {
        super(roseStacker, StackType.ENTITY, false);
    }

    @Override
    public void handleConversion(Set<ConversionData> conversionData) {
        for (ConversionData data : conversionData) {
            LivingEntity entity = data.getEntity();
            List<byte[]> entityStackData = this.createEntityStackNBT(entity.getType(), data.getStackSize(), entity.getLocation());
            this.stackManager.addEntityStack(new StackedEntity(data.getEntity(), entityStackData));
        }
    }

}
