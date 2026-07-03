---
name: jca-add-domain-event
description: Use when creating a domain event and its infrastructure listener — covers event record, entity registration, use case publishing, and the 3-annotation listener pattern.
---

# Creating a Domain Event and Listener

## 1. Event Record — `domain/<bc>/events/`

```java
package com.<project>.<bc>.events;

import com.<project>.domain.common.events.DomainEvent;
import java.util.UUID;

public record <Entity>CreatedEvent(UUID <entity>Id, String someField) implements DomainEvent {}
```

Rules:
- Must be a `record` implementing `DomainEvent`
- Include the entity ID and any data consumers will need
- No Spring dependencies, no infrastructure types

---

## 2. Register in Entity

Call `registerEvent()` inside `create()` or state-transition methods — NEVER in `reconstitute()`.

```java
// Inside Entity.create()
entity.registerEvent(new <Entity>CreatedEvent(entity.getId(), entity.getName().getValue()));

// Inside a transition method
private void transition(Consumer<<Entity>StateMachine> action) {
    var sm = <Entity>StateMachine.of(this);
    action.accept(sm);
    this.status = sm.getValue();
    var event = sm.getEvent();
    if (event != null) registerEvent(event);    // ← registered here
}
```

---

## 3. Publish in Use Case

After `repository.save()`, call `pullEvents()` to drain and publish. `pullEvents()` clears the list — events are published once.

```java
var saved = repository.save(entity);
saved.pullEvents().forEach(publisher::publish);
```

`DomainEventPublisher` must be injected in the use case (see `jca-add-use-case`).

---

## 4. Listener — `infrastructure/<bc>/listeners/<Bc>EventListener.java`

Three annotations are mandatory on every listener method — they work together:

```java
package com.<project>.infrastructure.<bc>.listeners;

import com.<project>.<bc>.events.<Entity>CreatedEvent;
import com.<project>.infrastructure.common.utils.EventLoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
public class <Bc>EventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(<Entity>CreatedEvent event) {
        EventLoggingUtils.logEventStart(event, "<entity>Id=" + event.<entity>Id());
        // side effects here: send emails, update read models, notify external services
        EventLoggingUtils.logEventEnd(event);
    }
}
```

**Why three annotations:**
- `@TransactionalEventListener(AFTER_COMMIT)` — only fires after the publishing transaction committed; prevents processing events from rolled-back transactions
- `@Async("eventExecutor")` — runs in `eventExecutor` thread pool, decoupled from the caller; `eventExecutor` is defined in `boot/config/async/AsyncConfig.java` — do NOT create another
- `@Transactional(REQUIRES_NEW)` — listener runs in its own transaction; failure does not affect the original operation

**No `@Component`** — register manually in boot.

---

## 5. Bean Registration — boot

Add to `boot/config/beans/infra/InfraListenerBeans.java`:

```java
@Bean
public <Bc>EventListener <bc>EventListener() {
    return new <Bc>EventListener();
}
```

If `InfraListenerBeans.java` doesn't exist yet:

```java
@Configuration
public class InfraListenerBeans {

    @Bean
    public <Bc>EventListener <bc>EventListener() {
        return new <Bc>EventListener();
    }
}
```

---

## Checklist

- [ ] Event is a `record implements DomainEvent` with entity ID
- [ ] `registerEvent()` called in entity (never in `reconstitute()`)
- [ ] `pullEvents().forEach(publisher::publish)` in use case after save
- [ ] Listener has all three annotations: `@TransactionalEventListener(AFTER_COMMIT)` + `@Async("eventExecutor")` + `@Transactional(REQUIRES_NEW)`
- [ ] `EventLoggingUtils.logEventStart/logEventEnd` used in every listener method
- [ ] No `@Component` on listener — `@Bean` in `InfraListenerBeans`
- [ ] No new `AsyncConfig` created — `eventExecutor` already exists
