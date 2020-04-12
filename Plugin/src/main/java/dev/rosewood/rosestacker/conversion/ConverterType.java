package dev.rosewood.rosestacker.conversion;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.conversion.handler.ConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.EntityConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.ItemConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.StackMobConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.UltimateStackerEntityConversionHandler;
import dev.rosewood.rosestacker.conversion.handler.UltimateStackerItemConversionHandler;

public enum ConverterType {

    ENTITY(EntityConversionHandler.class),
    ITEM(ItemConversionHandler.class),
    ULTIMATESTACKER_ENTITY(UltimateStackerEntityConversionHandler.class),
    ULTIMATESTACKER_ITEM(UltimateStackerItemConversionHandler.class),
    STACKMOB(StackMobConversionHandler.class);

    private final Class<? extends ConversionHandler> conversionHandler;

    ConverterType(Class<? extends ConversionHandler> conversionHandler) {
        this.conversionHandler = conversionHandler;
    }

    public ConversionHandler getConversionHandler() {
        try {
            return this.conversionHandler.getConstructor(RoseStacker.class).newInstance(RoseStacker.getInstance());
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
