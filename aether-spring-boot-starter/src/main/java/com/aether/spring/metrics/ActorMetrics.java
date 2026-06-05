package com.aether.spring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Micrometer metrics for an individual actor.
 */
public class ActorMetrics {

    private final MeterRegistry meterRegistry;
    private final String actorName;
    private final AtomicInteger mailboxSize;
    private final AtomicInteger state;

    private final ConcurrentHashMap<String, Counter> receivedCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> processedCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> failedCounters = new ConcurrentHashMap<>();

    public ActorMetrics(MeterRegistry meterRegistry, String actorName) {
        this.meterRegistry = meterRegistry;
        this.actorName = actorName;
        this.mailboxSize = new AtomicInteger(0);
        this.state = new AtomicInteger(0);

        Gauge.builder("aether.actor.mailbox.size", this.mailboxSize, AtomicInteger::get)
                .tag("actor", actorName)
                .register(meterRegistry);

        Gauge.builder("aether.actor.state", this.state, AtomicInteger::get)
                .tag("actor", actorName)
                .register(meterRegistry);
    }

    public void recordMessageReceived(String messageType) {
        getReceivedCounter(messageType).increment();
    }

    public void recordMessageProcessed(String messageType) {
        getProcessedCounter(messageType).increment();
    }

    public void recordMessageFailed(String messageType, String exception) {
        getFailedCounter(messageType, exception).increment();
    }

    public void updateMailboxSize(int size) {
        this.mailboxSize.set(size);
    }

    public void updateState(int state) {
        this.state.set(state);
    }

    private Counter getReceivedCounter(String messageType) {
        return receivedCounters.computeIfAbsent(messageType, mt ->
                Counter.builder("aether.actor.messages.received")
                        .tag("actor", actorName)
                        .tag("message_type", mt)
                        .register(meterRegistry));
    }

    private Counter getProcessedCounter(String messageType) {
        return processedCounters.computeIfAbsent(messageType, mt ->
                Counter.builder("aether.actor.messages.processed")
                        .tag("actor", actorName)
                        .tag("message_type", mt)
                        .register(meterRegistry));
    }

    private Counter getFailedCounter(String messageType, String exception) {
        String key = messageType + "|" + exception;
        return failedCounters.computeIfAbsent(key, k ->
                Counter.builder("aether.actor.messages.failed")
                        .tag("actor", actorName)
                        .tag("message_type", messageType)
                        .tag("exception", exception)
                        .register(meterRegistry));
    }
}
