package com.aether.actor;

import com.aether.spi.ActorRef;

/**
 * Provides contextual information and operations for an Actor during message processing.
 */
public interface ActorContext {

    /**
     * Returns the ActorRef representing this actor itself.
     */
    ActorRef self();

    /**
     * Returns the ActorRef of the actor that sent the current message.
     * May be null if the sender is not known.
     */
    ActorRef sender();

    /**
     * Changes the actor's behavior to the given behavior.
     * This allows actors to switch how they handle subsequent messages.
     */
    void become(Behavior behavior);
}
