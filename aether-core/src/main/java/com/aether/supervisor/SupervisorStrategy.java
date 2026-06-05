package com.aether.supervisor;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines the strategy a supervisor uses to handle child actor failures.
 * <p>
 * The strategy type determines how failures propagate among siblings:
 * <ul>
 *   <li>{@code ONE_FOR_ONE} - Only the failed child is affected.</li>
 *   <li>{@code ALL_FOR_ONE} - All children are restarted when any child fails.</li>
 *   <li>{@code REST_FOR_ONE} - All children started after the failed child are restarted.</li>
 * </ul>
 * <p>
 * The {@code maxRestarts} and {@code withinDuration} settings define a restart
 * rate limit. If a child exceeds this rate, the supervisor escalates the failure.
 * The {@code backoff} defines the delay before restarting a failed child.
 */
public final class SupervisorStrategy {

    /**
     * Defines how the supervisor handles failures among its children.
     */
    public enum Type {
        /**
         * Only the failed child actor is restarted.
         */
        ONE_FOR_ONE,

        /**
         * All child actors are restarted when any child fails.
         */
        ALL_FOR_ONE,

        /**
         * All child actors started after the failed one are restarted.
         */
        REST_FOR_ONE
    }

    public static final int DEFAULT_MAX_RESTARTS = 10;
    public static final Duration DEFAULT_WITHIN_DURATION = Duration.ofSeconds(10);
    public static final Duration DEFAULT_BACKOFF = Duration.ofMillis(100);

    private final Type strategyType;
    private final int maxRestarts;
    private final Duration withinDuration;
    private final Duration backoff;

    private SupervisorStrategy(Type strategyType, int maxRestarts, Duration withinDuration, Duration backoff) {
        this.strategyType = Objects.requireNonNull(strategyType, "strategyType must not be null");
        this.maxRestarts = maxRestarts;
        this.withinDuration = Objects.requireNonNull(withinDuration, "withinDuration must not be null");
        this.backoff = Objects.requireNonNull(backoff, "backoff must not be null");
    }

    /**
     * Creates a new builder with the default strategy settings.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a default strategy: ONE_FOR_ONE, maxRestarts=10, withinDuration=10s, backoff=100ms.
     */
    public static SupervisorStrategy defaults() {
        return new SupervisorStrategy(Type.ONE_FOR_ONE, DEFAULT_MAX_RESTARTS, DEFAULT_WITHIN_DURATION, DEFAULT_BACKOFF);
    }

    public Type getStrategyType() {
        return strategyType;
    }

    public int getMaxRestarts() {
        return maxRestarts;
    }

    public Duration getWithinDuration() {
        return withinDuration;
    }

    public Duration getBackoff() {
        return backoff;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupervisorStrategy)) return false;
        SupervisorStrategy that = (SupervisorStrategy) o;
        return maxRestarts == that.maxRestarts &&
                strategyType == that.strategyType &&
                withinDuration.equals(that.withinDuration) &&
                backoff.equals(that.backoff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strategyType, maxRestarts, withinDuration, backoff);
    }

    @Override
    public String toString() {
        return "SupervisorStrategy{" +
                "strategyType=" + strategyType +
                ", maxRestarts=" + maxRestarts +
                ", withinDuration=" + withinDuration +
                ", backoff=" + backoff +
                '}';
    }

    /**
     * Builder for constructing {@link SupervisorStrategy} instances.
     */
    public static final class Builder {
        private Type strategyType = Type.ONE_FOR_ONE;
        private int maxRestarts = DEFAULT_MAX_RESTARTS;
        private Duration withinDuration = DEFAULT_WITHIN_DURATION;
        private Duration backoff = DEFAULT_BACKOFF;

        private Builder() {
        }

        public Builder strategyType(Type strategyType) {
            this.strategyType = Objects.requireNonNull(strategyType, "strategyType must not be null");
            return this;
        }

        public Builder maxRestarts(int maxRestarts) {
            if (maxRestarts < 0) {
                throw new IllegalArgumentException("maxRestarts must be non-negative");
            }
            this.maxRestarts = maxRestarts;
            return this;
        }

        public Builder withinDuration(Duration withinDuration) {
            this.withinDuration = Objects.requireNonNull(withinDuration, "withinDuration must not be null");
            return this;
        }

        public Builder backoff(Duration backoff) {
            this.backoff = Objects.requireNonNull(backoff, "backoff must not be null");
            return this;
        }

        public SupervisorStrategy build() {
            return new SupervisorStrategy(strategyType, maxRestarts, withinDuration, backoff);
        }
    }
}
