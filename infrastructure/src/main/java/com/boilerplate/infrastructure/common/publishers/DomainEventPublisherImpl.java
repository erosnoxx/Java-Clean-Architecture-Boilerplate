package com.boilerplate.infrastructure.common.publishers;

import com.boilerplate.application.common.events.DomainEventPublisher;
import com.boilerplate.domain.common.events.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class DomainEventPublisherImpl implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
