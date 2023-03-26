package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConfigurationManager;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Debug {

    private static Logger logger;

    private Debug() { }

    /**
     * @return the debug Logger
     */
    private static Logger getLogger() {
        if (logger == null) {
            logger = new Logger("Debug", null) { };
            logger.setParent(RoseStacker.getInstance().getLogger());
            logger.setLevel(Level.ALL);
        }
        return logger;
    }

    /**
     * @return true if debug logging is enabled
     */
    public static boolean isLoggingEnabled() {
        return ConfigurationManager.Setting.DEBUG_LOGGING_ENABLED.getBoolean();
    }

    /**
     * Logs a message to the debug logger if logging is enabled and it passes the given condition
     *
     * @param condition the condition to check
     * @param messageSupplier the supplier for the message to log
     */
    public static void log(Supplier<Boolean> condition, Supplier<String> messageSupplier) {
        if (isLoggingEnabled() && condition.get()) {
            getLogger().log(Level.WARNING, messageSupplier.get());
            new Throwable("Printing stacktrace for debug purposes").printStackTrace();
        }
    }

    /**
     * Logs a message to the debug logger with no checks
     *
     * @param message the message to log
     */
    public static void log(String message) {
        getLogger().log(Level.WARNING, message);
        new Throwable("Printing stacktrace for debug purposes").printStackTrace();
    }

}
