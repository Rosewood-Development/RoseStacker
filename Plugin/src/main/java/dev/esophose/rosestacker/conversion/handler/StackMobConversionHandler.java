package dev.esophose.rosestacker.conversion.handler;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.ConversionData;
import dev.esophose.rosestacker.stack.StackType;
import dev.esophose.rosestacker.stack.StackedEntity;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class StackMobConversionHandler extends ConversionHandler {

    @SuppressWarnings("deprecated") // Need to use this constructor since we don't have a Plugin reference
    private static final NamespacedKey STACK_KEY = new NamespacedKey("stackmob", "stack-size");

    public StackMobConversionHandler(RoseStacker roseStacker) {
        super(roseStacker, StackType.ENTITY, true);
    }

    @Override
    public void handleConversion(Set<ConversionData> conversionData) {
        Set<LivingEntity> entities = conversionData.stream()
                .map(ConversionData::getEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        for (LivingEntity entity : entities) {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            int stackSize = dataContainer.getOrDefault(STACK_KEY, PersistentDataType.INTEGER, -1);
            if (stackSize == -1)
                continue;

            this.stackManager.addEntityStack(new StackedEntity(entity, this.createEntityStackNBT(entity.getType(), stackSize - 1, entity.getLocation())));
        }
    }

}
