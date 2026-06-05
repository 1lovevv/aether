package com.aether.spring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance metrics for monitoring actor system performance.
 * <p>
 * Tracks:
 * <ul>
 *   <li>Message throughput (messages per second)</li>
 *   <li>Processing latency (histogram)</li>
 *   <li>Mailbox wait time</li>
 *   <li>Actor creation/destruction rates</li>
 * </ul>
 */
public class PerformanceMetrics {

    private final MeterRegistry meterRegistry;

    // Throughput tracking
    private final Counter messagesSent;
    private final Counter messagesReceived;
    private final Counter messagesProcessed;
    private final Counter messagesFailed;

    // Latency tracking
    private final Timer processingTimer;
    private final Timer mailboxWaitTimer;
    private final Timer askResponseTimer;

    // Actor lifecycle
    private final Counter actorsCreated;
    private final Counter actorsDestroyed;
    private final Counter actorsRestarted;

    // Current state
    private final AtomicLong activeActors = new AtomicLong(0);
    private final AtomicLong pendingMessages = new AtomicLong(0);

    public PerformanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Throughput counters
        this.messagesSent = Counter.builder("aether.performance.messages.sent")
                .description("Total messages sent")
                .register(meterRegistry);

        this.messagesReceived = Counter.builder("aether.performance.messages.received")
                .description("Total messages received")
                .register(meterRegistry);

        this.messagesProcessed = Counter.builder("aether.performance.messages.processed")
                .description("Total messages processed successfully")
                .register(meterRegistry);

        this.messagesFailed = Counter.builder("aether.performance.messages.failed")
                .description("Total messages that failed processing")
                .register(meterRegistry);

        // Latency timers
        this.processingTimer = Timer.builder("aether.performance.processing.time")
                .description("Message processing time")
                .register(meterRegistry);

        this.mailboxWaitTimer = Timer.builder("aether.performance.mailbox.wait.time")
                .description("Time messages wait in mailbox before processing")
                .register(meterRegistry);

        this.askResponseTimer = Timer.builder("aether.performance.ask.response.time")
                .description("Response time for ask pattern")
                .register(meterRegistry);

        // Lifecycle counters
        this.actorsCreated = Counter.builder("aether.performance.actors.created")
                .description("Total actors created")
                .register(meterRegistry);

        this.actorsDestroyed = Counter.builder("aether.performance.actors.destroyed")
                .description("Total actors destroyed")
                .register(meterRegistry);

        this.actorsRestarted = Counter.builder("aether.performance.actors.restarted")
                .description("Total actors restarted by supervisor")
                .register(meterRegistry);
    }

    // Throughput methods
    public void recordMessageSent() {
        messagesSent.increment();
    }

    public void recordMessageReceived() {
        messagesReceived.increment();
    }

    public void recordMessageProcessed() {
        messagesProcessed.increment();
    }

    public void recordMessageFailed() {
        messagesFailed.increment();
    }

    // Latency methods
    public Timer.Sample startProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordProcessingTime(Timer.Sample sample) {
        sample.stop(processingTimer);
    }

    public Timer.Sample startMailboxWaitTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordMailboxWaitTime(Timer.Sample sample) {
        sample.stop(mailboxWaitTimer);
    }

    public Timer.Sample startAskTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordAskResponseTime(Timer.Sample sample) {
        sample.stop(askResponseTimer);
    }

    // Lifecycle methods
    public void recordActorCreated() {
        actorsCreated.increment();
        activeActors.incrementAndGet();
    }

    public void recordActorDestroyed() {
        actorsDestroyed.increment();
        activeActors.decrementAndGet();
    }

    public void recordActorRestarted() {
        actorsRestarted.increment();
    }

    // State methods
    public void incrementPendingMessages() {
        pendingMessages.incrementAndGet();
    }

    public void decrementPendingMessages() {
        pendingMessages.decrementAndGet();
    }

    public long getActiveActors() {
        return activeActors.get();
    }

    public long getPendingMessages() {
        return pendingMessages.get();
    }
}
