package com.aether.spring;

import com.aether.spi.ActorRef;
import com.aether.spi.ActorRegistry;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SpringActorRegistry implements ActorRegistry {

    private final ConcurrentHashMap<String, ActorRef> actors = new ConcurrentHashMap<>();

    @Override
    public void register(String id, ActorRef ref) {
        actors.put(id, ref);
    }

    @Override
    public Optional<ActorRef> findActor(String id) {
        return Optional.ofNullable(actors.get(id));
    }

    @Override
    public Collection<ActorRef> findActorsByType(Class<?> actorClass) {
        return actors.values().stream()
                .filter(ref -> ref.getClass().equals(actorClass))
                .collect(Collectors.toList());
    }
}
