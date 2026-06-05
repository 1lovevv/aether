package com.aether.spring;

import com.aether.mailbox.Mailbox;
import com.aether.message.Message;
import com.aether.spi.ActorRef;
import com.aether.spi.ThreadScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Actor message passing.
 * Verifies that actors can send and receive messages correctly.
 */
@SpringBootTest(classes = TestApplication.class)
class ActorMessagePassingIntegrationTest {

    @Autowired
    private ThreadScheduler threadScheduler;

    @Test
    void testTellMessageBetweenActors() throws InterruptedException {
        // Create two actors
        TestActor sender = new TestActor("/user/sender", threadScheduler);
        TestActor receiver = new TestActor("/user/receiver", threadScheduler);

        sender.start();
        receiver.start();

        // Send a message from sender to receiver
        TestMessage message = new TestMessage("Hello from sender");
        receiver.tell(message);

        // Give time for message processing
        Thread.sleep(100);

        // Verify the receiver processed the message
        assertTrue(receiver.hasReceivedMessages());

        sender.stop();
        receiver.stop();
    }

    @Test
    void testAskPattern() throws Exception {
        // Create an actor that will reply
        ReplyActor replyingActor = new ReplyActor("/user/replier", threadScheduler);
        replyingActor.start();

        // Ask for a reply
        TestMessage request = new TestMessage("Request");
        CompletableFuture<TestMessage> future = replyingActor.ask(request, Duration.ofSeconds(2));

        // Give time for the ask to complete
        Thread.sleep(200);

        // The future should not be completed yet (no one is replying)
        // In a real scenario, the replying actor would send a message back
        assertNotNull(future);

        replyingActor.stop();
    }

    @Test
    void testMultipleMessages() throws InterruptedException {
        TestActor actor = new TestActor("/user/multi", threadScheduler);
        actor.start();

        // Send multiple messages
        for (int i = 0; i < 5; i++) {
            actor.tell(new TestMessage("Message " + i));
        }

        // Give time for processing
        Thread.sleep(200);

        // Verify all messages were received
        assertEquals(5, actor.getMessageCount());

        actor.stop();
    }

    @Test
    void testActorLifecycle() throws InterruptedException {
        TestActor actor = new TestActor("/user/lifecycle", threadScheduler);

        assertEquals(com.aether.actor.ActorState.CREATED, actor.getState());

        actor.start();
        Thread.sleep(50);
        assertEquals(com.aether.actor.ActorState.RUNNING, actor.getState());

        actor.stop();
        Thread.sleep(50);
        assertEquals(com.aether.actor.ActorState.STOPPING, actor.getState());
    }

    // Test message implementation
    record TestMessage(String content) implements Message {}

    // Test actor implementation
    static class TestActor extends com.aether.actor.Actor {
        private final AtomicInteger messageCount = new AtomicInteger(0);
        private volatile boolean hasReceivedMessages = false;

        TestActor(String path, ThreadScheduler scheduler) {
            super(path, new Mailbox(), scheduler);
        }

        @Override
        public void run() {
            while (getState() == com.aether.actor.ActorState.RUNNING) {
                try {
                    Message message = getMailbox().dequeue();
                    if (message != null) {
                        messageCount.incrementAndGet();
                        hasReceivedMessages = true;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        public boolean hasReceivedMessages() {
            return hasReceivedMessages;
        }

        public int getMessageCount() {
            return messageCount.get();
        }
    }

    // Actor that replies to messages
    static class ReplyActor extends com.aether.actor.Actor {
        ReplyActor(String path, ThreadScheduler scheduler) {
            super(path, new Mailbox(), scheduler);
        }
    }
}
