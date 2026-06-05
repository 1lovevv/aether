package com.aether.actor;

import com.aether.mailbox.Mailbox;
import com.aether.message.Message;
import com.aether.spi.ActorRef;
import com.aether.spi.ThreadScheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Core Actor implementation with lifecycle management and message processing loop.
 * Implements both ActorRef (for messaging) and Runnable (for scheduling).
 */
public class Actor implements ActorRef, Runnable {

    private final String path;
    private final Mailbox mailbox;
    private final ThreadScheduler scheduler;
    private final AtomicReference<ActorState> state;

    private volatile Behavior currentBehavior;
    private volatile ActorRef currentSender;

    public Actor(String path, Mailbox mailbox, ThreadScheduler scheduler) {
        this.path = path;
        this.mailbox = mailbox;
        this.scheduler = scheduler;
        this.state = new AtomicReference<>(ActorState.CREATED);
    }

    /**
     * Starts the actor by transitioning from CREATED -> STARTING -> RUNNING
     * and scheduling it on the ThreadScheduler.
     */
    public void start() {
        if (!state.compareAndSet(ActorState.CREATED, ActorState.STARTING)) {
            throw new IllegalStateException("Actor can only be started from CREATED state, current state: " + state.get());
        }

        try {
            state.set(ActorState.RUNNING);
            scheduler.schedule(this);
        } catch (Exception e) {
            state.set(ActorState.FAILED);
            throw new RuntimeException("Failed to start actor: " + path, e);
        }
    }

    @Override
    public void run() {
        while (state.get() == ActorState.RUNNING) {
            try {
                Message message = mailbox.dequeue();
                if (message != null) {
                    processMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Log and continue processing; individual message failures should not stop the actor
                // In a full implementation, this would use a proper logging framework
                System.err.println("Error processing message in actor " + path + ": " + e.getMessage());
            }
        }

        // Transition to STOPPED when the loop exits
        if (state.compareAndSet(ActorState.RUNNING, ActorState.STOPPED)
                || state.compareAndSet(ActorState.STOPPING, ActorState.STOPPED)) {
            // Actor stopped cleanly
        }
    }

    /**
     * Processes a single message using the current behavior.
     */
    private void processMessage(Message message) {
        if (currentBehavior != null) {
            currentBehavior.receive(message, createContext());
        }
    }

    /**
     * Creates an ActorContext for the current message processing.
     */
    private ActorContext createContext() {
        Actor actor = this;
        return new ActorContext() {
            @Override
            public ActorRef self() {
                return actor;
            }

            @Override
            public ActorRef sender() {
                return currentSender;
            }

            @Override
            public void become(Behavior behavior) {
                actor.currentBehavior = behavior;
            }
        };
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public void tell(Message message) {
        ActorState current = state.get();
        if (current == ActorState.STOPPING || current == ActorState.STOPPED || current == ActorState.TERMINATED) {
            throw new IllegalStateException("Cannot send message to stopped/terminated actor: " + path);
        }
        try {
            mailbox.enqueue(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while enqueueing message to actor: " + path, e);
        }
    }

    @Override
    public <T extends Message> CompletableFuture<T> ask(Message message, Duration timeout) {
        CompletableFuture<T> future = new CompletableFuture<>();
        // MVP: simplified implementation - just tell and return a future
        // In a full implementation, this would set up a temporary response handler
        tell(message);
        return future;
    }

    /**
     * Signals the actor to stop processing messages.
     * Transitions to STOPPING state; the message loop will exit and transition to STOPPED.
     */
    public void stop() {
        ActorState current = state.get();
        if (current == ActorState.RUNNING || current == ActorState.STARTING) {
            state.set(ActorState.STOPPING);
            // Interrupt the mailbox dequeue to unblock the run loop
            // In a more complete implementation, we might send a PoisonPill message
        }
    }

    /**
     * Returns the current lifecycle state of this actor.
     */
    public ActorState getState() {
        return state.get();
    }

    /**
     * Returns the mailbox used by this actor.
     */
    public Mailbox getMailbox() {
        return mailbox;
    }

    /**
     * Sets the initial behavior for this actor.
     */
    public void setBehavior(Behavior behavior) {
        this.currentBehavior = behavior;
    }

    /**
     * Returns the current behavior of this actor.
     */
    public Behavior getBehavior() {
        return currentBehavior;
    }
}
