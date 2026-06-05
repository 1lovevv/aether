package com.aether.spring;

import com.aether.spi.ActorFactory;
import com.aether.spi.ActorRegistry;
import com.aether.spi.ThreadScheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(AetherProperties.class)
@EnableConfigurationProperties(AetherProperties.class)
public class AetherAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ActorFactory actorFactory(ApplicationContext applicationContext) {
        return new SpringActorFactory(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public ActorRegistry actorRegistry() {
        return new SpringActorRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThreadScheduler threadScheduler() {
        return new VirtualThreadScheduler();
    }
}
