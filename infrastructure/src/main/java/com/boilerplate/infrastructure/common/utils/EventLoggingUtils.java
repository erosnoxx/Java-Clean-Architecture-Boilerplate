package com.boilerplate.infrastructure.common.utils;

import com.boilerplate.domain.common.events.DomainEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventLoggingUtils {
    private EventLoggingUtils() {}

    public static void logEventStart(DomainEvent event, String details) {
        log.debug("processing event {} — {}", event.getClass().getSimpleName(), details);
    }

    public static void logEventEnd(DomainEvent event) {
        log.debug("event {} processed", event.getClass().getSimpleName());
    }
}
