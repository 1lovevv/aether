package com.aether.spring;

import com.aether.spring.metrics.ActorMetrics;
import com.aether.spring.metrics.AetherMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Micrometer metrics.
 * Verifies that actor and system metrics are properly recorded.
 */
class MetricsIntegrationTest {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @BeforeEach
    void setUp() {
        // Clear metrics before each test
        meterRegistry.clear();
    }

    @Test
    void testActorMetricsAreRegistered() {
        ActorMetrics actorMetrics = new ActorMetrics(meterRegistry, "test-actor");
        assertNotNull(actorMetrics);

        // Record some metrics
        actorMetrics.recordMessageReceived("TestMessage");
        actorMetrics.recordMessageProcessed("TestMessage");
        actorMetrics.recordMessageFailed("TestMessage", "RuntimeException");
        actorMetrics.updateMailboxSize(5);
        actorMetrics.updateState(1);

        // Verify metrics exist
        assertNotNull(meterRegistry.find("aether.actor.messages.received").counter());
        assertNotNull(meterRegistry.find("aether.actor.messages.processed").counter());
        assertNotNull(meterRegistry.find("aether.actor.messages.failed").counter());
        assertNotNull(meterRegistry.find("aether.actor.mailbox.size").gauge());
        assertNotNull(meterRegistry.find("aether.actor.state").gauge());
    }

    @Test
    void testSystemMetricsAreRegistered() {
        AetherMetrics aetherMetrics = new AetherMetrics(meterRegistry);
        assertNotNull(aetherMetrics);

        // Update metrics
        aetherMetrics.updateTotalActors(10);
        aetherMetrics.updateActiveActors(5);
        aetherMetrics.updatePinnedVirtualThreads(0);

        // Verify metrics exist
        assertNotNull(meterRegistry.find("aether.system.actors.total").gauge());
        assertNotNull(meterRegistry.find("aether.system.actors.active").gauge());
        assertNotNull(meterRegistry.find("aether.system.virtual_threads.pinned").gauge());
    }

    @Test
    void testActorMetricsCounterValues() {
        ActorMetrics actorMetrics = new ActorMetrics(meterRegistry, "counter-actor");

        // Record multiple messages
        actorMetrics.recordMessageReceived("Msg1");
        actorMetrics.recordMessageReceived("Msg1");
        actorMetrics.recordMessageReceived("Msg2");

        Counter receivedCounter = meterRegistry.find("aether.actor.messages.received")
                .tag("actor", "counter-actor")
                .tag("message_type", "Msg1")
                .counter();

        assertNotNull(receivedCounter);
        assertEquals(2.0, receivedCounter.count());
    }

    @Test
    void testSystemMetricsGaugeValues() {
        AetherMetrics aetherMetrics = new AetherMetrics(meterRegistry);

        aetherMetrics.updateTotalActors(100);
        aetherMetrics.updateActiveActors(50);
        aetherMetrics.updatePinnedVirtualThreads(2);

        Gauge totalGauge = meterRegistry.find("aether.system.actors.total").gauge();
        assertNotNull(totalGauge);
        assertEquals(100.0, totalGauge.value());

        Gauge activeGauge = meterRegistry.find("aether.system.actors.active").gauge();
        assertNotNull(activeGauge);
        assertEquals(50.0, activeGauge.value());

        Gauge pinnedGauge = meterRegistry.find("aether.system.virtual_threads.pinned").gauge();
        assertNotNull(pinnedGauge);
        assertEquals(2.0, pinnedGauge.value());
    }

    @Test
    void testMultipleActorsHaveSeparateMetrics() {
        ActorMetrics actor1 = new ActorMetrics(meterRegistry, "actor-1");
        ActorMetrics actor2 = new ActorMetrics(meterRegistry, "actor-2");

        actor1.recordMessageReceived("Msg");
        actor2.recordMessageReceived("Msg");
        actor2.recordMessageReceived("Msg");

        Counter counter1 = meterRegistry.find("aether.actor.messages.received")
                .tag("actor", "actor-1")
                .tag("message_type", "Msg")
                .counter();
        Counter counter2 = meterRegistry.find("aether.actor.messages.received")
                .tag("actor", "actor-2")
                .tag("message_type", "Msg")
                .counter();

        assertNotNull(counter1);
        assertNotNull(counter2);
        assertEquals(1.0, counter1.count());
        assertEquals(2.0, counter2.count());
    }
}
