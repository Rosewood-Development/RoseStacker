package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.manager.ConfigurationManager.Setting;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

/**
 * Handles converting data that we weren't able to without having specific locations
 */
public abstract class ConversionHandler {

    protected RosePlugin rosePlugin;
    protected StackManager stackManager;

    private StackType requiredDataStackType;
    private boolean useChunkEntities;

    public ConversionHandler(RosePlugin rosePlugin, StackType requiredDataStackType) {
        this(rosePlugin, requiredDataStackType, false);
    }

    public ConversionHandler(RosePlugin rosePlugin, StackType requiredDataStackType, boolean useChunkEntities) {
        this.rosePlugin = rosePlugin;
        this.stackManager = this.rosePlugin.getManager(StackManager.class);
        this.requiredDataStackType = requiredDataStackType;
        this.useChunkEntities = useChunkEntities;
    }

    public abstract Set<Stack<?>> handleConversion(Set<ConversionData> conversionData);

    public StackType getRequiredDataStackType() {
        return this.requiredDataStackType;
    }

    public boolean shouldUseChunkEntities() {
        return this.useChunkEntities;
    }

    /**
     * Used to fill in the missing entity stack nbt data
     *
     * @param entityType The type of entity
     * @param amount The amount of nbt entries to create
     * @param location The location of the main entity
     * @return A list of nbt data
     */
    protected List<byte[]> createEntityStackNBT(EntityType entityType, int amount, Location location) {
        List<byte[]> entityNBT = new LinkedList<>();

        NMSHandler nmsHandler = NMSAdapter.getHandler();
        for (int i = 0; i < amount - 1; i++)
            entityNBT.add(nmsHandler.getEntityAsNBT(nmsHandler.createEntityUnspawned(entityType, location), Setting.ENTITY_SAVE_ATTRIBUTES.getBoolean()));

        return Collections.synchronizedList(entityNBT);
    }

}
