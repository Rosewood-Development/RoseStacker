package dev.esophose.rosestacker.converter.handler;

import dev.esophose.rosestacker.RoseStacker;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;

public class StackMobConversionHandler extends ConversionHandler {

    @SuppressWarnings("deprecated") // Need to use this constructor since we don't have a Plugin reference
    private static final NamespacedKey STACK_KEY = new NamespacedKey("stackmob", "stack-size");

    public StackMobConversionHandler(RoseStacker roseStacker) {
        super(roseStacker);
    }

    @Override
    public void handleConversion(Set<Chunk> chunks) {

    }

}
