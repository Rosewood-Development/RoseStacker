package dev.rosewood.rosestacker.nms.object;

public class CompactNBTException extends RuntimeException {

    public CompactNBTException(Throwable cause) {
        super("An error occurred reading or writing NBT", cause);
    }

}
