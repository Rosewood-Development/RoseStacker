package dev.esophose.rosestacker.converter.handler;

import dev.esophose.rosestacker.RoseStacker;
import java.util.Set;
import org.bukkit.Chunk;

/**
 * Handles converting data that we weren't able to without having specific locations
 */
public abstract class ConversionHandler {

    private RoseStacker roseStacker;

    public ConversionHandler(RoseStacker roseStacker) {
        this.roseStacker = roseStacker;
    }

    public abstract void handleConversion(Set<Chunk> chunks);

}
