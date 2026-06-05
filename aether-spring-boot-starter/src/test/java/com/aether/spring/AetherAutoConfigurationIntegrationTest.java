package com.aether.spring;

import com.aether.spi.ActorFactory;
import com.aether.spi.ActorRegistry;
import com.aether.spi.ThreadScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Spring Boot auto-configuration.
 * Verifies that all Aether beans are properly created and configured.
 */
@SpringBootTest(classes = TestApplication.class)
class AetherAutoConfigurationIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AetherProperties aetherProperties;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void actorFactoryBeanIsCreated() {
        ActorFactory actorFactory = applicationContext.getBean(ActorFactory.class);
        assertNotNull(actorFactory);
        assertTrue(actorFactory instanceof SpringActorFactory);
    }

    @Test
    void actorRegistryBeanIsCreated() {
        ActorRegistry actorRegistry = applicationContext.getBean(ActorRegistry.class);
        assertNotNull(actorRegistry);
        assertTrue(actorRegistry instanceof SpringActorRegistry);
    }

    @Test
    void threadSchedulerBeanIsCreated() {
        ThreadScheduler threadScheduler = applicationContext.getBean(ThreadScheduler.class);
        assertNotNull(threadScheduler);
        assertTrue(threadScheduler instanceof VirtualThreadScheduler);
    }

    @Test
    void aetherPropertiesAreLoaded() {
        assertNotNull(aetherProperties);
        assertTrue(aetherProperties.getVirtualThreads().isEnabled());
        assertTrue(aetherProperties.getVirtualThreads().isPinningMonitoring());
        assertEquals(1000, aetherProperties.getActor().getDefaultMailboxCapacity());
        assertEquals("at-most-once", aetherProperties.getActor().getDefaultDeliverySemantics());
        assertEquals("one-for-one", aetherProperties.getSupervision().getDefaultStrategy());
        assertEquals(10, aetherProperties.getSupervision().getMaxRestarts());
        assertTrue(aetherProperties.getMetrics().isEnabled());
    }

    @Test
    void allBeansAreSingletons() {
        ActorFactory factory1 = applicationContext.getBean(ActorFactory.class);
        ActorFactory factory2 = applicationContext.getBean(ActorFactory.class);
        assertSame(factory1, factory2);

        ActorRegistry registry1 = applicationContext.getBean(ActorRegistry.class);
        ActorRegistry registry2 = applicationContext.getBean(ActorRegistry.class);
        assertSame(registry1, registry2);

        ThreadScheduler scheduler1 = applicationContext.getBean(ThreadScheduler.class);
        ThreadScheduler scheduler2 = applicationContext.getBean(ThreadScheduler.class);
        assertSame(scheduler1, scheduler2);
    }
}
