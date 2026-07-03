---
name: jca-add-entity
description: Use when creating a Domain Entity — enforces create()/reconstitute() factory pattern, encapsulation rules, event registration, and provides JPA counterpart template.
---

# Creating a Domain Entity

## Mandatory Pattern

Every domain entity must have:
1. `private` constructor — no public or package-private constructors
2. `static create(...)` — applies domain rules, sets initial state, may register events
3. `static reconstitute(...)` — pure state restore from persistence, NO events, NO validation

**Never** expose setters from the domain entity. Mutations happen via explicit domain methods (`rename()`, `changeEmail()`, `activate()`, etc.).

---

## Entity Template

```java
package com.<project>.<bc>.entities;

import com.<project>.domain.common.entities.DomainEntity;
import com.<project>.<bc>.events.<Entity>CreatedEvent;
import com.<project>.<bc>.vos.<Name>;
import lombok.Getter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class <Entity> extends DomainEntity<UUID> {

    private <Name> name;
    // ... other fields, all private, no @Setter

    private <Entity>() {}

    public static <Entity> create(<Name> name, ...) {
        // validate domain invariants here if needed
        var entity = new <Entity>();
        entity.setId(UUID.randomUUID());   // setId() is protected on DomainEntity
        entity.name = name;
        // set other initial fields
        entity.registerEvent(new <Entity>CreatedEvent(entity.getId()));
        return entity;
    }

    public static <Entity> reconstitute(
            UUID id,
            <Name> name,
            // ... all persisted fields ...
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        var entity = new <Entity>();
        entity.setId(id);
        entity.name = name;
        // set other fields
        entity.setTimestamps(createdAt, updatedAt);  // inherited from DomainEntity
        return entity;               // NO registerEvent() here
    }

    // Domain mutation methods — business logic lives here, not in use cases
    public void rename(<Name> name) {
        this.name = name;
    }
}
```

## Key Rules

- `setId(ID)` is available because `DomainEntity` declares `@Setter(AccessLevel.PROTECTED)` on `id`
- `setTimestamps(createdAt, updatedAt)` is a `protected` method on `DomainEntity` — call it only in `reconstitute()`
- `@Getter` on the class generates all getters; add `@Getter` on individual fields if class-level is not used
- Do NOT add `@Setter` anywhere on the entity class
- If the entity has state transitions, invoke `jca-add-state-machine`

---

## JPA Counterpart

The JPA entity lives in `infrastructure`, NOT in `domain`. They are separate classes.

```java
package com.<project>.infrastructure.<bc>.data.entities;

import com.<project>.infrastructure.common.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "<entities>")
@Getter @Setter @NoArgsConstructor
public class <Entity>Entity extends BaseJpaEntity<UUID> {

    @Column(nullable = false)
    private String name;

    // For enum fields:
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private <Status>Enum status;
}
```

`BaseJpaEntity<ID>` provides: `id`, `createdAt` (`@PrePersist`), `updatedAt` (`@PreUpdate`).

---

## EntityMapper

```java
package com.<project>.infrastructure.<bc>.mappers;

import com.<project>.infrastructure.common.mapper.EntityMapper;

public class <Entity>EntityMapper implements EntityMapper<<Entity>, <Entity>Entity> {

    @Override
    public <Entity>Entity toPersistence(<Entity> domain) {
        var entity = new <Entity>Entity();
        entity.setId(domain.getId());
        entity.setName(domain.getName().getValue());  // unwrap VO
        entity.setStatus(domain.getStatus());
        return entity;
    }

    @Override
    public <Entity> toDomain(<Entity>Entity persistence) {
        return <Entity>.reconstitute(
            persistence.getId(),
            <Name>.of(persistence.getName()),          // rewrap VO
            persistence.getStatus(),
            persistence.getCreatedAt(),
            persistence.getUpdatedAt()
        );
    }
}
```

---

## Test Checklist

```java
@Test void createSetsId() {
    var entity = <Entity>.create(<Name>.of("Example"), ...);
    assertThat(entity.getId()).isNotNull();
}

@Test void createRegistersEvent() {
    var entity = <Entity>.create(...);
    var events = entity.pullEvents();
    assertThat(events).hasSize(1);
    assertThat(events.getFirst()).isInstanceOf(<Entity>CreatedEvent.class);
}

@Test void pullEventsClearsQueue() {
    var entity = <Entity>.create(...);
    entity.pullEvents();
    assertThat(entity.pullEvents()).isEmpty();
}

@Test void reconstituteHasNoEvents() {
    var entity = <Entity>.reconstitute(UUID.randomUUID(), ..., null, null);
    assertThat(entity.pullEvents()).isEmpty();
}

@Test void reconstituteRestoresAllFields() {
    var id = UUID.randomUUID();
    var entity = <Entity>.reconstitute(id, <Name>.of("Test"), ..., createdAt, updatedAt);
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
}
```
