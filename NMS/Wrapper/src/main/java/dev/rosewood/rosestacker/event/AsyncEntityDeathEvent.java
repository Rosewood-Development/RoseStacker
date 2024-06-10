package dev.rosewood.rosestacker.event;

/**
 * Only called when trigger-death-event-for-entire-stack-kill is enabled in the config.
 * Called once per entity in the stack, may or may not be called async.
 */
public interface AsyncEntityDeathEvent {

}
