package com.aether.actor;

import com.aether.message.Message;

/**
 * Defines how an actor processes messages.
 * Implementations receive messages along with an ActorContext for self-reference,
 * sender access, and behavior switching.
 */
@FunctionalInterface
public interface Behavior {

    /**
     * Process an incoming message.
     *
     * @param message the message to process
     * @param context the actor's context during message processing
     */
    void receive(Message message, ActorContext context);
}
