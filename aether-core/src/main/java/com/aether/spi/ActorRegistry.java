package com.aether.spi;

import java.util.Collection;
import java.util.Optional;

public interface ActorRegistry {
    void register(String id, ActorRef ref);
    Optional<ActorRef> findActor(String id);
    Collection<ActorRef> findActorsByType(Class<?> actorClass);
}
