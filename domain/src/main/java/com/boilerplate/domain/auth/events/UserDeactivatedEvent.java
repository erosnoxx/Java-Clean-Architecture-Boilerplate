package com.boilerplate.domain.auth.events;

import com.boilerplate.domain.common.events.DomainEvent;
import java.util.UUID;

public record UserDeactivatedEvent(UUID userId) implements DomainEvent {}
