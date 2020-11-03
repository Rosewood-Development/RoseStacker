package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.LivingEntity;

public class EntityConversionHandler extends ConversionHandler {

    public EntityConversionHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackType.ENTITY);
    }

    @Override
    public Set<Stack<?>> handleConversion(Set<ConversionData> conversionData) {
        Set<Stack<?>> stacks = new HashSet<>();

        for (ConversionData data : conversionData) {
            LivingEntity entity = data.getEntity();
            entity.setCustomName(null); // This could cause data loss if the entity actually has a custom name, but we have to remove the stack tag
            entity.setCustomNameVisible(false);

            List<byte[]> entityStackData = this.createEntityStackNBT(entity.getType(), data.getStackSize(), entity.getLocation());
            StackedEntity stackedEntity = new StackedEntity(data.getEntity(), entityStackData);
            this.stackManager.addEntityStack(stackedEntity);
            stacks.add(stackedEntity);
        }

        return stacks;
    }

}
