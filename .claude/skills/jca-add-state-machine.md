---
name: jca-add-state-machine
description: Use when adding state transitions to an existing domain entity — covers the full 5-component pattern (enum, state interface, state impls, facade, entity integration) with test checklist.
---

# Implementing a State Machine

This pattern is built on `domain/common/statemachine/StateMachine<V>`. It replaces boolean flags (`active`, `approved`) with an explicit, type-safe state model where invalid transitions are rejected at the domain level.

## The 5 Components

### 1. Status Enum — `domain/<bc>/enums/<Entity>Status.java`

Each constant declares its own state singleton via an abstract method.

```java
package com.<project>.<bc>.enums;

import com.<project>.<bc>.states.<Entity>State;
import com.<project>.<bc>.states.Active<Entity>State;
import com.<project>.<bc>.states.Inactive<Entity>State;
import com.<project>.<bc>.states.Suspended<Entity>State;

public enum <Entity>Status {
    ACTIVE {
        @Override public <Entity>State getState() { return Active<Entity>State.INSTANCE; }
    },
    INACTIVE {
        @Override public <Entity>State getState() { return Inactive<Entity>State.INSTANCE; }
    },
    SUSPENDED {
        @Override public <Entity>State getState() { return Suspended<Entity>State.INSTANCE; }
    };

    public abstract <Entity>State getState();
}
```

### 2. State Interface — `domain/<bc>/states/<Entity>State.java`

One method per allowed operation. Parameters receive the StateMachine facade (not the entity directly).

```java
package com.<project>.<bc>.states;

import com.<project>.<bc>.statemachine.<Entity>StateMachine;

public interface <Entity>State {
    void activate(<Entity>StateMachine sm);
    void deactivate(<Entity>StateMachine sm);
    void suspend(<Entity>StateMachine sm);
}
```

### 3. State Implementations — `domain/<bc>/states/`

One class per status. Singleton pattern: `public static final INSTANCE`, `private` constructor.

- **Valid transitions**: call `sm.transition(newStatus, event)` to record the outcome
- **Invalid transitions**: throw `new DomainException("reason")`

```java
// Active<Entity>State.java
public class Active<Entity>State implements <Entity>State {
    public static final Active<Entity>State INSTANCE = new Active<Entity>State();
    private Active<Entity>State() {}

    @Override public void activate(<Entity>StateMachine sm) {
        throw new DomainException("<entity> is already active");
    }
    @Override public void deactivate(<Entity>StateMachine sm) {
        sm.transition(<Entity>Status.INACTIVE, new <Entity>DeactivatedEvent(sm.get<Entity>Id()));
    }
    @Override public void suspend(<Entity>StateMachine sm) {
        sm.transition(<Entity>Status.SUSPENDED, new <Entity>SuspendedEvent(sm.get<Entity>Id()));
    }
}

// Inactive<Entity>State.java
public class Inactive<Entity>State implements <Entity>State {
    public static final Inactive<Entity>State INSTANCE = new Inactive<Entity>State();
    private Inactive<Entity>State() {}

    @Override public void activate(<Entity>StateMachine sm) {
        sm.transition(<Entity>Status.ACTIVE, new <Entity>ActivatedEvent(sm.get<Entity>Id()));
    }
    @Override public void deactivate(<Entity>StateMachine sm) {
        throw new DomainException("<entity> is already inactive");
    }
    @Override public void suspend(<Entity>StateMachine sm) {
        throw new DomainException("cannot suspend an inactive <entity>");
    }
}

// Suspended<Entity>State.java — activate allowed, deactivate/suspend throw
public class Suspended<Entity>State implements <Entity>State {
    public static final Suspended<Entity>State INSTANCE = new Suspended<Entity>State();
    private Suspended<Entity>State() {}

    @Override public void activate(<Entity>StateMachine sm) {
        sm.transition(<Entity>Status.ACTIVE, new <Entity>ActivatedEvent(sm.get<Entity>Id()));
    }
    @Override public void deactivate(<Entity>StateMachine sm) {
        throw new DomainException("<entity> is suspended, cannot deactivate directly");
    }
    @Override public void suspend(<Entity>StateMachine sm) {
        throw new DomainException("<entity> is already suspended");
    }
}
```

### 4. StateMachine Facade — `domain/<bc>/statemachine/<Entity>StateMachine.java`

```java
package com.<project>.<bc>.statemachine;

import com.<project>.<bc>.entities.<Entity>;
import com.<project>.<bc>.enums.<Entity>Status;
import com.<project>.domain.common.events.DomainEvent;
import com.<project>.domain.common.statemachine.StateMachine;
import java.util.UUID;

public class <Entity>StateMachine extends StateMachine<<Entity>Status> {

    private final UUID <entity>Id;

    private <Entity>StateMachine(<Entity>Status status, UUID <entity>Id) {
        this.value = status;
        this.<entity>Id = <entity>Id;
    }

    public static <Entity>StateMachine of(<Entity> entity) {
        return new <Entity>StateMachine(entity.getStatus(), entity.getId());
    }

    public UUID get<Entity>Id() { return <entity>Id; }

    /** Called by state implementations to record the transition result. */
    public void transition(<Entity>Status newStatus, DomainEvent event) {
        this.value = newStatus;
        this.event = event;
    }

    public void activate()   { value.getState().activate(this); }
    public void deactivate() { value.getState().deactivate(this); }
    public void suspend()    { value.getState().suspend(this); }
}
```

### 5. Entity Integration — modifications to `<Entity>.java`

```java
// Replace boolean flag with status enum
private <Entity>Status status;

// Private transition helper — the only way state changes happen
private void transition(java.util.function.Consumer<<Entity>StateMachine> action) {
    var sm = <Entity>StateMachine.of(this);
    action.accept(sm);
    this.status = sm.getValue();
    var event = sm.getEvent();
    if (event != null) registerEvent(event);
}

// Public API — semantic names, not generic toggle
public void activate()   { transition(<Entity>StateMachine::activate); }
public void deactivate() { transition(<Entity>StateMachine::deactivate); }
public void suspend()    { transition(<Entity>StateMachine::suspend); }

// Convenience accessor (optional)
public boolean isActive() { return status == <Entity>Status.ACTIVE; }
```

Update `create()`: `entity.status = <Entity>Status.ACTIVE;`
Update `reconstitute()`: accept `<Entity>Status status` parameter instead of `boolean active`.

---

## JPA — Status Column

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private <Entity>Status status;
```

Update `isEnabled()` if entity implements `UserDetails`:
```java
@Override public boolean isEnabled() { return status == <Entity>Status.ACTIVE; }
```

---

## Domain Events for Transitions

Create one event per meaningful transition — invoke `jca-add-domain-event`:

```java
public record <Entity>ActivatedEvent(UUID <entity>Id) implements DomainEvent {}
public record <Entity>DeactivatedEvent(UUID <entity>Id) implements DomainEvent {}
public record <Entity>SuspendedEvent(UUID <entity>Id) implements DomainEvent {}
```

---

## Test Checklist

```java
@Test void activateFromInactive() {
    var entity = /* create and then deactivate */;
    entity.activate();
    assertThat(entity.getStatus()).isEqualTo(<Entity>Status.ACTIVE);
    assertThat(entity.pullEvents()).hasSize(1)
        .first().isInstanceOf(<Entity>ActivatedEvent.class);
}

@Test void deactivateFromActive() {
    var entity = <Entity>.create(...);  // starts ACTIVE
    entity.deactivate();
    assertThat(entity.getStatus()).isEqualTo(<Entity>Status.INACTIVE);
}

@Test void suspendFromActive() {
    var entity = <Entity>.create(...);
    entity.suspend();
    assertThat(entity.getStatus()).isEqualTo(<Entity>Status.SUSPENDED);
}

@Test void activateWhenAlreadyActiveThrows() {
    var entity = <Entity>.create(...);
    assertThatThrownBy(entity::activate).isInstanceOf(DomainException.class);
}

@Test void deactivateWhenInactiveThrows() {
    var entity = <Entity>.create(...);
    entity.deactivate();
    assertThatThrownBy(entity::deactivate).isInstanceOf(DomainException.class);
}

@Test void suspendWhenInactiveThrows() {
    var entity = <Entity>.create(...);
    entity.deactivate();
    assertThatThrownBy(entity::suspend).isInstanceOf(DomainException.class);
}
```
