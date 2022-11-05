package dev.rosewood.rosestacker.command.type;

import org.bukkit.Material;

public class StackedBlockMaterial {

    private final Material material;

    public StackedBlockMaterial(Material material) {
        this.material = material;
    }

    public Material get() {
        return this.material;
    }

}
