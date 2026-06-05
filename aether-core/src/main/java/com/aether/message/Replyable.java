package com.aether.message;

import com.aether.spi.ActorRef;

/**
 * Marker interface for messages that can be sent with a reply expectation.
 * When using {@code ask()}, the sent message should typically implement this interface
 * so the receiving actor knows where to send the reply.
 */
public interface Replyable extends Message {

    /**
     * Returns the ActorRef to which the reply should be sent.
     * This is typically set automatically by the ask() implementation.
     */
    ActorRef getReplyTo();

    /**
     * Sets the ActorRef to which the reply should be sent.
     */
    void setReplyTo(ActorRef replyTo);
}
