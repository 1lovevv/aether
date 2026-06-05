package com.aether.spi;

public interface ActorFactory {
    <T> T createActor(Class<T> actorClass, String name);
    void destroyActor(String name);
}
