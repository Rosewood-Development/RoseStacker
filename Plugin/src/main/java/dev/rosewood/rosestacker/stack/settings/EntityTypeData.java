package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosestacker.utils.ItemUtils;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Contains data loaded from entity_data.json
 */
public record EntityTypeData(
        boolean swimmingMob,
        boolean flyingMob,
        Material spawnEggMaterial,
        List<String> defaultSpawnRequirements,
        String skullTexture,
        Set<Material> breedingMaterials,
        String spawnCategory,
        Set<Material> standardEquipment
) {

    public ItemStack getSkullItem() {
        return ItemUtils.getCustomSkull(this.skullTexture);
    }

    public boolean isValidBreedingMaterial(Material material) {
        return this.breedingMaterials.contains(material);
    }

    public boolean isStandardEquipment(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null)
            return this.standardEquipment.contains(itemStack.getType());

        return !meta.hasEnchants() && this.standardEquipment.contains(itemStack.getType());
    }

}
