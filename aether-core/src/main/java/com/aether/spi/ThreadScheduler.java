package com.aether.spi;

public interface ThreadScheduler {
    void schedule(Runnable task);
    void shutdown();
}
