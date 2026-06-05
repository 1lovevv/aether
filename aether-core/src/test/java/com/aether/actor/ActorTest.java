package com.aether.actor;

import com.aether.mailbox.Mailbox;
import com.aether.message.Message;
import com.aether.spi.ActorRef;
import com.aether.spi.ThreadScheduler;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ActorTest {

    /**
     * Inline mock implementation of ThreadScheduler for testing.
     */
    private ThreadScheduler createMockScheduler(AtomicReference<Runnable> scheduledTaskRef, CountDownLatch latch) {
        return new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
                scheduledTaskRef.set(task);
                latch.countDown();
            }

            @Override
            public void shutdown() {
                // No-op for test
            }
        };
    }

    @Test
    void testLifecycleCreatedToRunning() throws InterruptedException {
        AtomicReference<Runnable> scheduledTask = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        ThreadScheduler scheduler = createMockScheduler(scheduledTask, latch);

        Mailbox mailbox = new Mailbox();
        Actor actor = new Actor("/test/actor1", mailbox, scheduler);

        assertEquals(ActorState.CREATED, actor.getState());

        actor.start();

        assertEquals(ActorState.RUNNING, actor.getState());
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Scheduler should have been called");
        assertNotNull(scheduledTask.get(), "Runnable should have been scheduled");
        assertSame(actor, scheduledTask.get(), "Scheduled runnable should be the actor itself");
    }

    @Test
    void testStartOnlyFromCreatedState() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
            }

            @Override
            public void shutdown() {
            }
        };

        Mailbox mailbox = new Mailbox();
        Actor actor = new Actor("/test/actor2", mailbox, scheduler);

        actor.start();
        assertEquals(ActorState.RUNNING, actor.getState());

        // Starting again should throw
        assertThrows(IllegalStateException.class, actor::start);
    }

    @Test
    void testTellDoesNotThrow() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
            }

            @Override
            public void shutdown() {
            }
        };

        Mailbox mailbox = new Mailbox();
        Actor actor = new Actor("/test/actor3", mailbox, scheduler);

        Message msg = new Message() {};

        // tell() should not throw even before start
        assertDoesNotThrow(() -> actor.tell(msg));
        assertEquals(1, actor.getMailbox().size());
    }

    @Test
    void testTellAfterStart() throws InterruptedException {
        AtomicReference<Runnable> scheduledTask = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        ThreadScheduler scheduler = createMockScheduler(scheduledTask, latch);

        Mailbox mailbox = new Mailbox();
        Actor actor = new Actor("/test/actor4", mailbox, scheduler);

        actor.start();
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        Message msg = new Message() {};
        assertDoesNotThrow(() -> actor.tell(msg));
        assertEquals(1, actor.getMailbox().size());
    }

    @Test
    void testAskReturnsCompletableFuture() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
            }

            @Override
            public void shutdown() {
            }
        };

        Mailbox mailbox = new Mailbox();
        Actor actor = new Actor("/test/actor5", mailbox, scheduler);

        Message msg = new Message() {};
        CompletableFuture<? extends Message> future = actor.ask(msg, Duration.ofSeconds(1));

        assertNotNull(future);
        assertEquals(1, actor.getMailbox().size());
    }

    @Test
    void testStopTransitionsToStopping() {
        AtomicReference<Runnable> scheduledTask = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        ThreadScheduler scheduler = createMockScheduler(scheduledTask, latch);

        Mailbox mailbox = new Mailbox();
        Actor actor = new Actor("/test/actor6", mailbox, scheduler);

        actor.start();
        assertEquals(ActorState.RUNNING, actor.getState());

        actor.stop();
        assertEquals(ActorState.STOPPING, actor.getState());
    }

    @Test
    void testPath() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
            }

            @Override
            public void shutdown() {
            }
        };

        Actor actor = new Actor("/test/actor7", new Mailbox(), scheduler);
        assertEquals("/test/actor7", actor.path());
    }

    @Test
    void testTellToStoppedActorThrows() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
            }

            @Override
            public void shutdown() {
            }
        };

        Mailbox mailbox = new Mailbox();
        Actor actor = new Actor("/test/actor8", mailbox, scheduler);

        // Start then stop the actor to get it into STOPPING state
        actor.start();
        actor.stop(); // sets STOPPING
        // After stop(), tell() should throw because state is STOPPING
        assertThrows(IllegalStateException.class, () -> actor.tell(new Message() {}));
    }

    @Test
    void testBehaviorBecome() {
        ThreadScheduler scheduler = new ThreadScheduler() {
            @Override
            public void schedule(Runnable task) {
            }

            @Override
            public void shutdown() {
            }
        };

        Mailbox mailbox = new Mailbox();
        Actor actor = new Actor("/test/actor9", mailbox, scheduler);

        AtomicReference<String> received = new AtomicReference<>();
        Behavior behavior = (msg, ctx) -> received.set("handled");

        actor.setBehavior(behavior);
        assertSame(behavior, actor.getBehavior());
    }
}
