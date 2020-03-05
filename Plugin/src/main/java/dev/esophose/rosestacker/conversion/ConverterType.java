package dev.esophose.rosestacker.conversion;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.conversion.handler.ConversionHandler;
import dev.esophose.rosestacker.conversion.handler.EntityConversionHandler;
import dev.esophose.rosestacker.conversion.handler.ItemConversionHandler;
import dev.esophose.rosestacker.conversion.handler.StackMobConversionHandler;
import dev.esophose.rosestacker.conversion.handler.UltimateStackerEntityConversionHandler;
import dev.esophose.rosestacker.conversion.handler.UltimateStackerItemConversionHandler;

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
