package dev.rosewood.rosestacker.conversion.handler;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.ConversionData;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.storage.StackedEntityDataStorage;
import dev.rosewood.rosestacker.stack.Stack;
import dev.rosewood.rosestacker.stack.StackType;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Handles converting data that we weren't able to without having specific locations
 */
public abstract class ConversionHandler {

    protected RosePlugin rosePlugin;
    protected StackManager stackManager;

    private final StackType requiredDataStackType;
    private final boolean useChunkEntities;

    public ConversionHandler(RosePlugin rosePlugin, StackType requiredDataStackType) {
        this(rosePlugin, requiredDataStackType, false);
    }

    public ConversionHandler(RosePlugin rosePlugin, StackType requiredDataStackType, boolean useChunkEntities) {
        this.rosePlugin = rosePlugin;
        this.stackManager = this.rosePlugin.getManager(StackManager.class);
        this.requiredDataStackType = requiredDataStackType;
        this.useChunkEntities = useChunkEntities;
    }

    /**
     * Handles the conversion of data
     *
     * @param conversionData The conversion data from the other stacker plugin
     * @return any newly created stacks from the conversion data
     */
    public abstract Set<Stack<?>> handleConversion(Set<ConversionData> conversionData);

    /**
     * @return the stack type that this conversion handler handles
     */
    public StackType getRequiredDataStackType() {
        return this.requiredDataStackType;
    }

    /**
     * @return true if the conversion data set of {@link #handleConversion(Set)} should be populated with all chunk entities
     */
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
    protected StackedEntityDataStorage createEntityStackNBT(EntityType entityType, int amount, Location location) {
        NMSHandler nmsHandler = NMSAdapter.getHandler();
        StackedEntityDataStorage stackedEntityDataStorage = nmsHandler.createEntityDataStorage(nmsHandler.createNewEntityUnspawned(entityType, location, CreatureSpawnEvent.SpawnReason.CUSTOM), RoseStacker.getInstance().getManager(StackManager.class).getEntityDataStorageType());
        for (int i = 0; i < amount - 1; i++)
            stackedEntityDataStorage.add(nmsHandler.createNewEntityUnspawned(entityType, location, CreatureSpawnEvent.SpawnReason.CUSTOM));

        return stackedEntityDataStorage;
    }

}
