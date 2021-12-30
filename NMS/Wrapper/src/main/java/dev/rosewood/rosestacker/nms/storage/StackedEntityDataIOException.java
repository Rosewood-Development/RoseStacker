package dev.rosewood.rosestacker.nms.storage;

public class StackedEntityDataIOException extends RuntimeException {

    public StackedEntityDataIOException(Throwable cause) {
        super("An error occurred reading or writing stacked entity data", cause);
    }

}
