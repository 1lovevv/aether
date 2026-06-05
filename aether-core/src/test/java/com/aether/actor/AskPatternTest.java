package com.aether.actor;

import com.aether.mailbox.Mailbox;
import com.aether.message.Message;
import com.aether.spi.ThreadScheduler;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ask() pattern implementation.
 */
class AskPatternTest {

    record TestMessage(String content) implements Message {}
    record ReplyMessage(String response) implements Message {}

    @Test
    void testAskReturnsCompletableFuture() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
                new Thread(task).start();
            }
            @Override
            public void shutdown() {}
        };

        Actor actor = new Actor("/user/test", new Mailbox(), scheduler);
        actor.start();

        CompletableFuture<ReplyMessage> future = actor.ask(new TestMessage("hello"), Duration.ofSeconds(5));
        assertNotNull(future);
        assertFalse(future.isDone());

        actor.stop();
    }

    @Test
    void testAskWithTimeout() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
                new Thread(task).start();
            }
            @Override
            public void shutdown() {}
        };

        Actor actor = new Actor("/user/test-timeout", new Mailbox(), scheduler);
        actor.start();

        // This should timeout since no one will reply
        CompletableFuture<ReplyMessage> future = actor.ask(new TestMessage("timeout-test"), Duration.ofMillis(100));

        assertThrows(java.util.concurrent.TimeoutException.class, () -> {
            try {
                future.get(500, TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.ExecutionException e) {
                throw e.getCause();
            }
        });

        actor.stop();
    }

    @Test
    void testAskPatternHelper() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
                new Thread(task).start();
            }
            @Override
            public void shutdown() {}
        };

        Actor sender = new Actor("/user/sender", new Mailbox(), scheduler);
        sender.start();

        Actor target = new Actor("/user/target", new Mailbox(), scheduler);
        target.start();

        // Test that AskPattern can be created
        AskPattern pattern = AskPattern.ask(sender, Duration.ofSeconds(5));
        assertNotNull(pattern);

        sender.stop();
        target.stop();
    }
}
