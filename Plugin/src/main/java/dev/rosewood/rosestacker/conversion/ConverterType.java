package dev.rosewood.rosestacker.conversion;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.handler.ConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.EntityConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.ItemConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.StackMobConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.UltimateStackerConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.WildStackerEntityConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.WildStackerItemConversionHandler;

public enum ConverterType {

    ENTITY(EntityConversionHandler.class),
    ITEM(ItemConversionHandler.class),
    ULTIMATESTACKER(UltimateStackerConversionHandler.class),
    STACKMOB(StackMobConversionHandler.class),
    WS_ENTITY(WildStackerEntityConversionHandler.class),
    WS_ITEM(WildStackerItemConversionHandler.class);

    private final Class<? extends ConversionHandler> conversionHandler;

    ConverterType(Class<? extends ConversionHandler> conversionHandler) {
        this.conversionHandler = conversionHandler;
    }

    /**
     * @return a newly created ConversionHandler instance for this type
     */
    public ConversionHandler getConversionHandler() {
        try {
            return this.conversionHandler.getConstructor(RosePlugin.class).newInstance(RoseStacker.getInstance());
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a ConverterType by its name
     *
     * @param name The name of the ConverterType
     * @return The ConverterType, or null if not found
     */
    public static ConverterType get(String name) {
        if (name.equalsIgnoreCase("ULTIMATESTACKER_ENTITY")) { // legacy compatibility with pre-1.1.4 versions
            return ConverterType.ULTIMATESTACKER;
        } else {
            try {
                return ConverterType.valueOf(name.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }

}
