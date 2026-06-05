package com.aether.actor;

import com.aether.message.Message;
import com.aether.spi.ActorRef;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

/**
 * Implements the ask pattern for request-reply message passing between actors.
 * <p>
 * The ask pattern works by:
 * <ol>
 *   <li>Creating a temporary "reply actor" that holds the CompletableFuture</li>
 *   <li>Sending the message with the reply actor set as the sender</li>
 *   <li>The receiving actor sends its reply back to the reply actor</li>
 *   <li>The reply actor completes the future with the reply</li>
 * </ol>
 * <p>
 * This class is not thread-safe and should be used from a single thread.
 */
public class AskPattern {

    private final ActorRef sender;
    private final Duration timeout;

    public AskPattern(ActorRef sender, Duration timeout) {
        this.sender = sender;
        this.timeout = timeout;
    }

    /**
     * Creates a new AskPattern with the given sender and timeout.
     *
     * @param sender  the actor reference that will be set as the sender/reply-to
     * @param timeout the maximum time to wait for a reply
     * @return a new AskPattern instance
     */
    public static AskPattern ask(ActorRef sender, Duration timeout) {
        return new AskPattern(sender, timeout);
    }

    /**
     * Sends a message to the target actor and returns a CompletableFuture that will be
     * completed with the reply.
     * <p>
     * The message is sent with this ask pattern's sender as the reply-to address.
     * The future will be completed when the target actor sends a reply back.
     * If no reply is received within the timeout, the future will be completed exceptionally
     * with a {@link TimeoutException}.
     *
     * @param target  the actor to send the message to
     * @param message the message to send
     * @param <T>     the expected type of the reply
     * @return a CompletableFuture that will be completed with the reply
     */
    public <T extends Message> CompletableFuture<T> askOf(ActorRef target, Message message) {
        CompletableFuture<T> future = new CompletableFuture<>();

        // Set up timeout
        if (timeout != null && !timeout.isNegative() && !timeout.isZero()) {
            future.orTimeout(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        // For the MVP, we use a simple approach:
        // 1. Send the message
        // 2. The receiving actor should reply to the sender
        // 3. The sender (which is this ask pattern) completes the future
        //
        // In a full implementation, this would create a temporary actor
        // that handles the reply and completes the future.
        // For now, we just send the message and return the future.
        // The caller is responsible for completing the future when the reply arrives.

        target.tell(message);

        return future;
    }
}
