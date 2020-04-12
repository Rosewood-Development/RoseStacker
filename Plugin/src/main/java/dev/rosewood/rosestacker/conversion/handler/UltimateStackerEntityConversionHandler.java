package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedEntity;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.LivingEntity;

public class UltimateStackerEntityConversionHandler extends UltimateStackerConversionHandler {

    public UltimateStackerEntityConversionHandler(RoseStacker roseStacker) {
        super(roseStacker, StackType.ENTITY, true);
    }

    @Override
    public void handleConversion(Set<ConversionData> conversionData) {
        for (ConversionData data : conversionData) {
            LivingEntity entity = data.getEntity();
            int stackSize = this.getEntityAmount(entity);
            if (stackSize == -1)
                continue;

            List<byte[]> entityStackData = this.createEntityStackNBT(entity.getType(), stackSize, entity.getLocation());
            this.stackManager.addEntityStack(new StackedEntity(data.getEntity(), entityStackData));
        }
    }

}
