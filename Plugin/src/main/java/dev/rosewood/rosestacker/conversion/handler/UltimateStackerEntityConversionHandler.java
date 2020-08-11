package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.LivingEntity;

public class UltimateStackerEntityConversionHandler extends UltimateStackerConversionHandler {

    public UltimateStackerEntityConversionHandler(RoseStacker roseStacker) {
        super(roseStacker, StackType.ENTITY);
    }

    @Override
    public Set<Stack<?>> handleConversion(Set<ConversionData> conversionData) {
        Set<Stack<?>> stacks = new HashSet<>();

        for (ConversionData data : conversionData) {
            LivingEntity entity = data.getEntity();
            if (entity == null)
                continue;

            int stackSize = this.getEntityAmount(entity);
            if (stackSize == -1)
                continue;

            List<byte[]> entityStackData = this.createEntityStackNBT(entity.getType(), stackSize, entity.getLocation());
            StackedEntity stackedEntity = new StackedEntity(data.getEntity(), entityStackData);
            this.stackManager.addEntityStack(stackedEntity);
            stacks.add(stackedEntity);
        }

        return stacks;
    }

}
