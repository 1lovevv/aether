package com.aether.spring;

import com.aether.spi.ActorFactory;
import org.springframework.context.ApplicationContext;

public class SpringActorFactory implements ActorFactory {

    private final ApplicationContext applicationContext;

    public SpringActorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T createActor(Class<T> actorClass, String name) {
        return applicationContext.getBean(actorClass);
    }

    @Override
    public void destroyActor(String name) {
        // No-op: Spring manages lifecycle
    }
}
