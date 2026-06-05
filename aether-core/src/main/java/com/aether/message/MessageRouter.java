package com.aether.message;

import com.aether.spi.ActorRef;

public interface MessageRouter {
    void route(Message message, ActorRef target);
}
