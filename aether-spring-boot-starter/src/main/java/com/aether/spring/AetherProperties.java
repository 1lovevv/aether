package com.aether.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aether")
public class AetherProperties {

    private VirtualThreads virtualThreads = new VirtualThreads();
    private Actor actor = new Actor();
    private Supervision supervision = new Supervision();
    private Metrics metrics = new Metrics();

    public VirtualThreads getVirtualThreads() {
        return virtualThreads;
    }

    public void setVirtualThreads(VirtualThreads virtualThreads) {
        this.virtualThreads = virtualThreads;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public Supervision getSupervision() {
        return supervision;
    }

    public void setSupervision(Supervision supervision) {
        this.supervision = supervision;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public static class VirtualThreads {
        private boolean enabled = true;
        private boolean pinningMonitoring = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isPinningMonitoring() {
            return pinningMonitoring;
        }

        public void setPinningMonitoring(boolean pinningMonitoring) {
            this.pinningMonitoring = pinningMonitoring;
        }
    }

    public static class Actor {
        private int defaultMailboxCapacity = 1000;
        private String defaultDeliverySemantics = "at-most-once";

        public int getDefaultMailboxCapacity() {
            return defaultMailboxCapacity;
        }

        public void setDefaultMailboxCapacity(int defaultMailboxCapacity) {
            this.defaultMailboxCapacity = defaultMailboxCapacity;
        }

        public String getDefaultDeliverySemantics() {
            return defaultDeliverySemantics;
        }

        public void setDefaultDeliverySemantics(String defaultDeliverySemantics) {
            this.defaultDeliverySemantics = defaultDeliverySemantics;
        }
    }

    public static class Supervision {
        private String defaultStrategy = "one-for-one";
        private int maxRestarts = 10;
        private String restartWindow = "10s";

        public String getDefaultStrategy() {
            return defaultStrategy;
        }

        public void setDefaultStrategy(String defaultStrategy) {
            this.defaultStrategy = defaultStrategy;
        }

        public int getMaxRestarts() {
            return maxRestarts;
        }

        public void setMaxRestarts(int maxRestarts) {
            this.maxRestarts = maxRestarts;
        }

        public String getRestartWindow() {
            return restartWindow;
        }

        public void setRestartWindow(String restartWindow) {
            this.restartWindow = restartWindow;
        }
    }

    public static class Metrics {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
