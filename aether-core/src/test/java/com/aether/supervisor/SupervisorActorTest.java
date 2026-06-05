package com.aether.supervisor;

import com.aether.actor.Actor;
import com.aether.actor.ActorState;
import com.aether.mailbox.Mailbox;
import com.aether.spi.ActorRef;
import com.aether.spi.ThreadScheduler;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SupervisorStrategy} and {@link SupervisorActor}.
 */
class SupervisorActorTest {

    // ------------------------------------------------------------------
    // SupervisorStrategy builder tests
    // ------------------------------------------------------------------

    @Test
    void defaultStrategyHasExpectedValues() {
        SupervisorStrategy strategy = SupervisorStrategy.defaults();

        assertEquals(SupervisorStrategy.Type.ONE_FOR_ONE, strategy.getStrategyType());
        assertEquals(10, strategy.getMaxRestarts());
        assertEquals(Duration.ofSeconds(10), strategy.getWithinDuration());
        assertEquals(Duration.ofMillis(100), strategy.getBackoff());
    }

    @Test
    void builderCreatesCustomStrategy() {
        SupervisorStrategy strategy = SupervisorStrategy.builder()
                .strategyType(SupervisorStrategy.Type.ALL_FOR_ONE)
                .maxRestarts(5)
                .withinDuration(Duration.ofSeconds(30))
                .backoff(Duration.ofMillis(500))
                .build();

        assertEquals(SupervisorStrategy.Type.ALL_FOR_ONE, strategy.getStrategyType());
        assertEquals(5, strategy.getMaxRestarts());
        assertEquals(Duration.ofSeconds(30), strategy.getWithinDuration());
        assertEquals(Duration.ofMillis(500), strategy.getBackoff());
    }

    @Test
    void builderDefaultsMatchDefaultsFactory() {
        SupervisorStrategy built = SupervisorStrategy.builder().build();
        SupervisorStrategy defaulted = SupervisorStrategy.defaults();

        assertEquals(defaulted, built);
    }

    @Test
    void builderRejectsNegativeMaxRestarts() {
        assertThrows(IllegalArgumentException.class, () ->
                SupervisorStrategy.builder().maxRestarts(-1).build()
        );
    }

    @Test
    void builderRejectsNullStrategyType() {
        assertThrows(NullPointerException.class, () ->
                SupervisorStrategy.builder().strategyType(null).build()
        );
    }

    @Test
    void builderRejectsNullWithinDuration() {
        assertThrows(NullPointerException.class, () ->
                SupervisorStrategy.builder().withinDuration(null).build()
        );
    }

    @Test
    void builderRejectsNullBackoff() {
        assertThrows(NullPointerException.class, () ->
                SupervisorStrategy.builder().backoff(null).build()
        );
    }

    // ------------------------------------------------------------------
    // SupervisorActor creation tests
    // ------------------------------------------------------------------

    @Test
    void supervisorActorCreatedWithDefaults() {
        Mailbox mailbox = mock(Mailbox.class);
        ThreadScheduler scheduler = mock(ThreadScheduler.class);

        SupervisorActor supervisor = new SupervisorActor("/supervisor", mailbox, scheduler);

        assertNotNull(supervisor);
        assertEquals("/supervisor", supervisor.path());
        assertEquals(SupervisorStrategy.defaults(), supervisor.getStrategy());
        assertTrue(supervisor.getChildren().isEmpty());
    }

    @Test
    void supervisorActorCreatedWithCustomStrategy() {
        Mailbox mailbox = mock(Mailbox.class);
        ThreadScheduler scheduler = mock(ThreadScheduler.class);
        SupervisorStrategy custom = SupervisorStrategy.builder()
                .strategyType(SupervisorStrategy.Type.REST_FOR_ONE)
                .maxRestarts(3)
                .build();

        SupervisorActor supervisor = new SupervisorActor("/supervisor", mailbox, scheduler, custom);

        assertEquals(SupervisorStrategy.Type.REST_FOR_ONE, supervisor.getStrategy().getStrategyType());
        assertEquals(3, supervisor.getStrategy().getMaxRestarts());
    }

    @Test
    void superviseRegistersChild() {
        Mailbox mailbox = mock(Mailbox.class);
        ThreadScheduler scheduler = mock(ThreadScheduler.class);
        SupervisorActor supervisor = new SupervisorActor("/supervisor", mailbox, scheduler);

        SupervisorActor.ChildActor child = supervisor.supervise(Actor.class, "child-1");

        assertNotNull(child);
        assertEquals("child-1", child.getName());
        assertEquals(Actor.class, child.getActorClass());
        assertEquals(ActorState.CREATED, child.getState());
        assertEquals(0, child.getRestartCount());
        assertEquals(1, supervisor.getChildren().size());
        assertTrue(supervisor.getChildren().containsKey("child-1"));
    }

    @Test
    void handleFailureReturnsRestartForMvp() {
        Mailbox mailbox = mock(Mailbox.class);
        ThreadScheduler scheduler = mock(ThreadScheduler.class);
        ActorRef childRef = mock(ActorRef.class);
        SupervisorActor supervisor = new SupervisorActor("/supervisor", mailbox, scheduler);
        supervisor.supervise(Actor.class, "child-1");

        SupervisorDirective directive = supervisor.handleFailure(childRef, new RuntimeException("boom"));

        assertEquals(SupervisorDirective.RESTART, directive);
    }

    @Test
    void handleFailureIncrementsRestartCount() {
        Mailbox mailbox = mock(Mailbox.class);
        ThreadScheduler scheduler = mock(ThreadScheduler.class);
        ActorRef childRef = mock(ActorRef.class);
        SupervisorActor supervisor = new SupervisorActor("/supervisor", mailbox, scheduler);
        SupervisorActor.ChildActor child = supervisor.supervise(Actor.class, "child-1");
        child.setRef(childRef);

        supervisor.handleFailure(childRef, new RuntimeException("boom"));

        assertEquals(1, child.getRestartCount());
        assertEquals(ActorState.STARTING, child.getState());
    }

    @Test
    void multipleChildrenCanBeRegistered() {
        Mailbox mailbox = mock(Mailbox.class);
        ThreadScheduler scheduler = mock(ThreadScheduler.class);
        SupervisorActor supervisor = new SupervisorActor("/supervisor", mailbox, scheduler);

        supervisor.supervise(Actor.class, "child-1");
        supervisor.supervise(Actor.class, "child-2");
        supervisor.supervise(Actor.class, "child-3");

        assertEquals(3, supervisor.getChildren().size());
    }

    @Test
    void supervisorDirectiveEnumValuesExist() {
        SupervisorDirective[] directives = SupervisorDirective.values();

        assertEquals(4, directives.length);
        assertNotNull(SupervisorDirective.RESTART);
        assertNotNull(SupervisorDirective.STOP);
        assertNotNull(SupervisorDirective.ESCALATE);
        assertNotNull(SupervisorDirective.TERMINATE);
    }
}
