package dev.esophose.rosestacker.conversion.handler;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.ConversionData;
import dev.esophose.rosestacker.stack.StackType;
import dev.esophose.rosestacker.stack.StackedEntity;
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

            List<String> entityStackData = this.createEntityStackNBT(entity.getType(), stackSize, entity.getLocation());
            this.stackManager.addEntityStack(new StackedEntity(data.getEntity(), entityStackData));
        }
    }

}
