package dev.esophose.sparkstacker.hook;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.utils.StackerUtils;
import net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SparkStackerSpawnerProvider implements ExternalSpawnerProvider {

    private SparkStacker sparkStacker;

    public SparkStackerSpawnerProvider(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
    }

    @Override
    public String getName() {
        return this.sparkStacker.getName();
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
