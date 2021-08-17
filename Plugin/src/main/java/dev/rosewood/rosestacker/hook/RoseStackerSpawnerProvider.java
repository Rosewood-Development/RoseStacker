package dev.rosewood.rosestacker.hook;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.utils.ItemUtils;
import net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class RoseStackerSpawnerProvider implements ExternalSpawnerProvider {

    private final RosePlugin rosePlugin;

    public RoseStackerSpawnerProvider(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @Override
    public String getName() {
        return this.rosePlugin.getName();
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType) {
        return ItemUtils.getSpawnerAsStackedItemStack(entityType, 1);
    }

    @Override
    public EntityType getSpawnerEntityType(ItemStack itemStack) {
        return ItemUtils.getStackedItemEntityType(itemStack);
    }

}
