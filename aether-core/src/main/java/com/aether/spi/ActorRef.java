package com.aether.spi;

import com.aether.message.Message;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface ActorRef {
    String path();
    void tell(Message message);
    <T extends Message> CompletableFuture<T> ask(Message message, Duration timeout);
}
