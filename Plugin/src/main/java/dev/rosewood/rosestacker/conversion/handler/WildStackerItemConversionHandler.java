package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedItem;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WildStackerItemConversionHandler extends ConversionHandler {

    private static final NamespacedKey STACK_KEY = new NamespacedKey("wildstacker", "stackamount");
    private static final NamespacedKey CONVERTED_KEY = new NamespacedKey(RoseStacker.getInstance(), "converted");

    public WildStackerItemConversionHandler(RosePlugin rosePlugin) {
        super(rosePlugin, StackType.ITEM, true);
    }

    @Override
    public Set<Stack<?>> handleConversion(Set<ConversionData> conversionData) {
        Set<Item> items = conversionData.stream()
                .map(ConversionData::getItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Stack<?>> stacks = new HashSet<>();

        for (Item item : items) {
            PersistentDataContainer dataContainer = item.getPersistentDataContainer();
            if (dataContainer.has(CONVERTED_KEY, PersistentDataType.INTEGER))
                continue;

            int stackSize = dataContainer.getOrDefault(STACK_KEY, PersistentDataType.INTEGER, -1);
            if (stackSize == -1)
                continue;

            dataContainer.set(CONVERTED_KEY, PersistentDataType.INTEGER, 1);
            StackedItem stackedItem = new StackedItem(stackSize, item);
            this.stackManager.addItemStack(stackedItem);
            stacks.add(stackedItem);
        }

        return stacks;
    }

}
