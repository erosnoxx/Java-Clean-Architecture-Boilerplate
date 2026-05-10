package com.boilerplate.application.common.events;

import com.boilerplate.domain.common.events.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
