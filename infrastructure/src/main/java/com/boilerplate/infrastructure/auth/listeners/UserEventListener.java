package com.boilerplate.infrastructure.auth.listeners;

import com.boilerplate.domain.auth.events.UserCreatedEvent;
import com.boilerplate.infrastructure.common.utils.EventLoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
public class UserEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(UserCreatedEvent event) {
        EventLoggingUtils.logEventStart(event, "userId=" + event.userId());
        // placeholder: send welcome email, provision defaults, etc.
        EventLoggingUtils.logEventEnd(event);
    }
}
