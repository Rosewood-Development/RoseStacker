package dev.rosewood.rosestacker.conversion;

import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

public class ConversionData {

    private LivingEntity entity;
    private Item item;
    private int stackSize;
    private UUID uuid;

    private ConversionData(LivingEntity entity, Item item, int stackSize, UUID uuid) {
        this.entity = entity;
        this.item = item;
        this.stackSize = stackSize;
        this.uuid = uuid;
    }

    public ConversionData(UUID uuid, int stackSize) {
        this(null, null, stackSize, uuid);
    }

    public ConversionData(Entity entity, int stackSize) {
        this(entity instanceof LivingEntity ? (LivingEntity) entity : null,
                entity instanceof Item ? (Item) entity : null,
                stackSize, null);
    }

    public ConversionData(Entity entity) {
        this(entity, -1);
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public Item getItem() {
        return this.item;
    }

    public int getStackSize() {
        return this.stackSize;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

}
