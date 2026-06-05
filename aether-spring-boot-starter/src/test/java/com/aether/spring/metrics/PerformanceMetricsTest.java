package com.aether.spring.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PerformanceMetrics}.
 */
class PerformanceMetricsTest {

    private MeterRegistry meterRegistry;
    private PerformanceMetrics performanceMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        performanceMetrics = new PerformanceMetrics(meterRegistry);
    }

    @Test
    void testMessageCounters() {
        performanceMetrics.recordMessageSent();
        performanceMetrics.recordMessageReceived();
        performanceMetrics.recordMessageProcessed();
        performanceMetrics.recordMessageFailed();

        assertNotNull(meterRegistry.find("aether.performance.messages.sent").counter());
        assertNotNull(meterRegistry.find("aether.performance.messages.received").counter());
        assertNotNull(meterRegistry.find("aether.performance.messages.processed").counter());
        assertNotNull(meterRegistry.find("aether.performance.messages.failed").counter());
    }

    @Test
    void testActorLifecycle() {
        performanceMetrics.recordActorCreated();
        assertEquals(1, performanceMetrics.getActiveActors());

        performanceMetrics.recordActorCreated();
        assertEquals(2, performanceMetrics.getActiveActors());

        performanceMetrics.recordActorDestroyed();
        assertEquals(1, performanceMetrics.getActiveActors());

        performanceMetrics.recordActorRestarted();
        assertNotNull(meterRegistry.find("aether.performance.actors.restarted").counter());
    }

    @Test
    void testPendingMessages() {
        performanceMetrics.incrementPendingMessages();
        performanceMetrics.incrementPendingMessages();
        assertEquals(2, performanceMetrics.getPendingMessages());

        performanceMetrics.decrementPendingMessages();
        assertEquals(1, performanceMetrics.getPendingMessages());
    }

    @Test
    void testTimers() {
        // Test processing timer
        var processingSample = performanceMetrics.startProcessingTimer();
        assertNotNull(processingSample);
        performanceMetrics.recordProcessingTime(processingSample);
        assertNotNull(meterRegistry.find("aether.performance.processing.time").timer());

        // Test mailbox wait timer
        var mailboxSample = performanceMetrics.startMailboxWaitTimer();
        assertNotNull(mailboxSample);
        performanceMetrics.recordMailboxWaitTime(mailboxSample);
        assertNotNull(meterRegistry.find("aether.performance.mailbox.wait.time").timer());

        // Test ask response timer
        var askSample = performanceMetrics.startAskTimer();
        assertNotNull(askSample);
        performanceMetrics.recordAskResponseTime(askSample);
        assertNotNull(meterRegistry.find("aether.performance.ask.response.time").timer());
    }
}
