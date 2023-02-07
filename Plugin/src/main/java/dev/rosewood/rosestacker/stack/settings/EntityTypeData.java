package dev.rosewood.rosestacker.stack.settings;

import dev.rosewood.rosestacker.utils.ItemUtils;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Contains data loaded from entity_data.json
 */
public record EntityTypeData(
        boolean swimmingMob,
        boolean flyingMob,
        Material spawnEggMaterial,
        List<String> defaultSpawnRequirements,
        String skullTexture,
        List<Material> breedingMaterials,
        String spawnCategory
) {

    public ItemStack getSkullItem() {
        return ItemUtils.getCustomSkull(this.skullTexture);
    }

    public boolean isValidBreedingMaterial(Material material) {
        return this.breedingMaterials.contains(material);
    }

}
