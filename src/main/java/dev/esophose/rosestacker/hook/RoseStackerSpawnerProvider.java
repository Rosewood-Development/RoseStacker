package dev.esophose.rosestacker.hook;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.utils.StackerUtils;
import net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class RoseStackerSpawnerProvider implements ExternalSpawnerProvider {

    private RoseStacker roseStacker;

    public RoseStackerSpawnerProvider(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    @Override
    public String getName() {
        return this.roseStacker.getName();
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType) {
        return StackerUtils.getSpawnerAsStackedItemStack(entityType, 1);
    }

    @Override
    public EntityType getSpawnerEntityType(ItemStack itemStack) {
        return StackerUtils.getStackedItemEntityType(itemStack);
    }
}
