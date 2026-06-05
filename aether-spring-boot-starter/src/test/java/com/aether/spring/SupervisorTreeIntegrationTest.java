package com.aether.spring;

import com.aether.actor.ActorState;
import com.aether.mailbox.Mailbox;
import com.aether.message.Message;
import com.aether.spi.ThreadScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Supervisor tree.
 * Verifies that supervisors can manage child actors and handle failures.
 */
@SpringBootTest(classes = TestApplication.class)
class SupervisorTreeIntegrationTest {

    @Autowired
    private ThreadScheduler threadScheduler;

    @Test
    void testSupervisorManagesChildren() {
        // Create a supervisor
        TestSupervisor supervisor = new TestSupervisor("/user/supervisor", threadScheduler);
        supervisor.start();

        // Register a child
        supervisor.supervise(TestChildActor.class, "child-1");

        // Verify child is registered
        assertEquals(1, supervisor.getChildren().size());
        assertNotNull(supervisor.getChildren().get("child-1"));

        supervisor.stop();
    }

    @Test
    void testSupervisorHandlesFailure() {
        // Create a supervisor
        TestSupervisor supervisor = new TestSupervisor("/user/supervisor-failure", threadScheduler);
        supervisor.start();

        // Register a child
        supervisor.supervise(TestChildActor.class, "failing-child");

        // Simulate a failure
        TestChildActor child = new TestChildActor("/user/failing-child", threadScheduler);
        child.start();

        // The supervisor should handle the failure
        var directive = supervisor.handleFailure(child, new RuntimeException("Test failure"));
        assertNotNull(directive);

        supervisor.stop();
        child.stop();
    }

    @Test
    void testSupervisorStrategyDefaults() {
        TestSupervisor supervisor = new TestSupervisor("/user/supervisor-strategy", threadScheduler);
        assertNotNull(supervisor.getStrategy());
        assertEquals(com.aether.supervisor.SupervisorStrategy.Type.ONE_FOR_ONE, supervisor.getStrategy().getStrategyType());
        assertEquals(10, supervisor.getStrategy().getMaxRestarts());
    }

    @Test
    void testMultipleChildren() {
        TestSupervisor supervisor = new TestSupervisor("/user/supervisor-multi", threadScheduler);
        supervisor.start();

        // Register multiple children
        supervisor.supervise(TestChildActor.class, "child-1");
        supervisor.supervise(TestChildActor.class, "child-2");
        supervisor.supervise(TestChildActor.class, "child-3");

        // Verify all children are registered
        assertEquals(3, supervisor.getChildren().size());
        assertNotNull(supervisor.getChildren().get("child-1"));
        assertNotNull(supervisor.getChildren().get("child-2"));
        assertNotNull(supervisor.getChildren().get("child-3"));

        supervisor.stop();
    }

    // Test supervisor implementation
    static class TestSupervisor extends com.aether.supervisor.SupervisorActor {
        TestSupervisor(String path, ThreadScheduler scheduler) {
            super(path, new Mailbox(), scheduler);
        }
    }

    // Test child actor implementation
    static class TestChildActor extends com.aether.actor.Actor {
        TestChildActor(String path, ThreadScheduler scheduler) {
            super(path, new Mailbox(), scheduler);
        }
    }
}
