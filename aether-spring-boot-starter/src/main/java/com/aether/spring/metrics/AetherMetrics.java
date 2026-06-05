package com.aether.spring.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Micrometer metrics for the overall Aether system.
 */
public class AetherMetrics {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger totalActors;
    private final AtomicInteger activeActors;
    private final AtomicInteger pinnedVirtualThreads;

    public AetherMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.totalActors = new AtomicInteger(0);
        this.activeActors = new AtomicInteger(0);
        this.pinnedVirtualThreads = new AtomicInteger(0);

        Gauge.builder("aether.system.actors.total", this.totalActors, AtomicInteger::get)
                .register(meterRegistry);

        Gauge.builder("aether.system.actors.active", this.activeActors, AtomicInteger::get)
                .register(meterRegistry);

        Gauge.builder("aether.system.virtual_threads.pinned", this.pinnedVirtualThreads, AtomicInteger::get)
                .register(meterRegistry);
    }

    public void updateTotalActors(int count) {
        this.totalActors.set(count);
    }

    public void updateActiveActors(int count) {
        this.activeActors.set(count);
    }

    public void updatePinnedVirtualThreads(int count) {
        this.pinnedVirtualThreads.set(count);
    }
}
