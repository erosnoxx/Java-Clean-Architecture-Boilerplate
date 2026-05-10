package com.boilerplate.boot.config.beans.infra;

import com.boilerplate.application.common.events.DomainEventPublisher;
import com.boilerplate.infrastructure.common.publishers.DomainEventPublisherImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InfraPublisherBeans {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return new DomainEventPublisherImpl(applicationEventPublisher);
    }

}
