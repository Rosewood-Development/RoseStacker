package dev.rosewood.rosestacker.gui;

import java.util.List;
import org.bukkit.Material;

/* package */ class GuiHelper {

    /**
     * Parses a material name into a Material, or replaces it with a banner if it's invalid
     *
     * @param materialName The name of the material
     * @return the material name parsed into a Material
     */
    public static Material parseMaterial(String materialName) {
        Material material = Material.matchMaterial(materialName);
        if (material == null)
            return Material.BARRIER;
        return material;
    }

    /**
     * Used to split a list of Strings into a display name a lore list
     */
    public static class GuiStringHelper {

        private final List<String> message;

        public GuiStringHelper(List<String> message) {
            this.message = message;
        }

        /**
         * @return the name of the item
         */
        public String getName() {
            return this.message.get(0);
        }

        /**
         * @return the lore of the item
         */
        public List<String> getLore() {
            if (this.message.size() == 1)
                return List.of();
            return this.message.subList(1, this.message.size());
        }

    }

}
