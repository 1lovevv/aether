package com.aether.actor;

import com.aether.mailbox.Mailbox;
import com.aether.message.Message;
import com.aether.spi.ActorRef;
import com.aether.spi.ThreadScheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A temporary actor used internally to handle replies in the ask() pattern.
 * <p>
 * This actor is created for each ask() call, receives the reply message,
 * completes the CompletableFuture, and then terminates itself.
 * <p>
 * This is an internal implementation detail and should not be used directly.
 */
class ReplyActor extends Actor {

    private final CompletableFuture<? super Message> future;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    ReplyActor(String path, Mailbox mailbox, ThreadScheduler scheduler, CompletableFuture future) {
        super(path, mailbox, scheduler);
        this.future = future;
    }

    @Override
    public void run() {
        try {
            Message message = getMailbox().dequeue();
            if (message != null && completed.compareAndSet(false, true)) {
                future.complete(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (completed.compareAndSet(false, true)) {
                future.completeExceptionally(new RuntimeException("Reply actor interrupted", e));
            }
        }
    }

    /**
     * Returns whether this reply actor has already completed its future.
     */
    public boolean isCompleted() {
        return completed.get();
    }
}
