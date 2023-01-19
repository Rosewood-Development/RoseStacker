package dev.rosewood.rosestacker.stack.settings;

import java.util.List;
import org.bukkit.Material;

/**
 * Contains data loaded from entity_data.json
 */
public record EntityTypeData(boolean isSwimmingMob, boolean isFlyingMob, Material spawnEggMaterial,
                             List<String> defaultSpawnRequirements, String skullTexture,
                             List<Material> breedingMaterials, String spawnCategory) {

}
