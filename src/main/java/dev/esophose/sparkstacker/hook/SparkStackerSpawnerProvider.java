package dev.esophose.sparkstacker.hook;

import dev.esophose.sparkstacker.SparkStacker;
import dev.esophose.sparkstacker.stack.settings.SpawnerStackSettings;
import dev.esophose.sparkstacker.utils.StackerUtils;
import net.brcdev.shopgui.provider.spawner.SpawnerProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
public class SparkStackerSpawnerProvider extends SpawnerProvider {

    private SparkStacker sparkStacker;

    public SparkStackerSpawnerProvider(SparkStacker sparkStacker) {
        this.sparkStacker = sparkStacker;
        this.hook(sparkStacker);
    }

    @Override
    public SpawnerProvider hook(Plugin plugin) {
        return this;
    }

    @Override
    public ItemStack getSpawnerItem(String entityId, String customName) {
        try {
            EntityType entityType = EntityType.fromName(entityId);
            ItemStack itemStack = StackerUtils.getEntityAsStackedItemStack(entityType, 1);

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && customName != null) {
                itemMeta.setDisplayName(customName);
                itemStack.setItemMeta(itemMeta);
            }

            return itemStack;
        } catch (Exception ignored) { }

        return null;
    }

    @Override
    public String getSpawnerEntityId(ItemStack itemStack) {
        try {
            EntityType entityType = StackerUtils.getStackedItemEntityType(itemStack);
            if (entityType != null)
                return entityType.getName();
        } catch (Exception ignored) { }

        return null;
    }

    @Override
    public String getSpawnerEntityName(ItemStack itemStack) {
        try {
            EntityType entityType = StackerUtils.getStackedItemEntityType(itemStack);
            if (entityType != null) {
                SpawnerStackSettings stackSettings = this.sparkStacker.getStackSettingManager().getSpawnerStackSettings(entityType);
                return stackSettings.getDisplayName();
            }
        } catch (Exception ignored) { }

        return null;
    }

}
