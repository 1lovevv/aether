package com.aether.spring;

import com.aether.spi.ThreadScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadScheduler implements ThreadScheduler {

    private final ExecutorService executor;

    public VirtualThreadScheduler() {
        ExecutorService exec;
        try {
            exec = (ExecutorService) Executors.class
                    .getMethod("newVirtualThreadPerTaskExecutor")
                    .invoke(null);
        } catch (ReflectiveOperationException e) {
            exec = Executors.newCachedThreadPool();
        }
        this.executor = exec;
    }

    @Override
    public void schedule(Runnable task) {
        executor.submit(task);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
