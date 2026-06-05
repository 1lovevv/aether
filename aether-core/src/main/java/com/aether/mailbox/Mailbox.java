package com.aether.mailbox;

import com.aether.message.Message;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Mailbox {

    private final LinkedBlockingQueue<Message> queue;
    private final int capacity;

    public Mailbox() {
        this.queue = new LinkedBlockingQueue<>();
        this.capacity = Integer.MAX_VALUE;
    }

    public Mailbox(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.capacity = capacity;
    }

    public void enqueue(Message message) throws InterruptedException {
        queue.put(message);
    }

    public Message dequeue() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }
}
