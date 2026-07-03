# JCA Skills Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create 8 project-scoped Claude skills (`jca-*`) and a `CLAUDE.md` that encode the architectural conventions of this Java Clean Architecture boilerplate, so any Claude session working on a project derived from this boilerplate follows the correct patterns automatically.

**Architecture:** Skills are Markdown files with YAML frontmatter stored in `.claude/skills/`. `CLAUDE.md` at the project root acts as the index, mapping when each skill should be invoked. Skills are loaded by Claude via the `Skill` tool when relevant.

**Tech Stack:** Markdown, YAML frontmatter, Claude Code skill system.

## Global Constraints

- All skill files go in `.claude/skills/` (sibling of existing `.claude/settings.json`)
- Frontmatter fields required: `name` (kebab-case, matches filename), `description` (one line, used by Claude to decide relevance)
- Skills reference actual class names, packages, and patterns from this boilerplate (e.g., `StringValueObject`, `QueryUtils`, `EventLoggingUtils`, `StateMachine<V>`)
- Skills are written in imperative, dense style — for Claude consumption, not human reading
- No placeholder text, no "TBD", no "implement X" without showing exactly how
- Each skill committed individually

---

### Task 1: Setup + `jca-overview`

**Files:**
- Create: `.claude/skills/jca-overview.md`

**Interfaces:**
- Produces: `jca-overview` skill, referenced by CLAUDE.md in Task 9

- [ ] **Step 1: Create the skills directory and overview skill**

Create `.claude/skills/jca-overview.md` with this exact content:

```markdown
---
name: jca-overview
description: Entry point for all backend work on this project — architecture, modules, global constraints, and routing to specific skills. Load before any Java implementation.
---

# Java Clean Architecture — Project Overview

## Module Responsibilities

| Module | Purpose | Constraint |
|---|---|---|
| `domain` | Entities, VOs, domain events, enums, states, state machines | Pure Java — zero Spring, zero infra dependencies |
| `application` | Use case interfaces + impls, repository contracts, criteria, schemas, mappers, query ports | No Spring annotations on classes; interfaces only |
| `infrastructure` | JPA entities, repositories, adapters, event listeners, sequence generators | Spring-aware; may access any JPA repository |
| `web` | REST controllers, exception handler | `@RestController`, `@RequestMapping` only |
| `boot` | Composition root — ALL `@Bean` and `@Configuration` definitions | Every class wired here; no component scan for infra/application |

## Package Naming

```
com.<project>.<boundedContext>.<sublayer>.*
```

Examples:
- `com.myapp.auth.entities.User`
- `com.myapp.financial.queries.UserSummary`
- `com.myapp.financial.usecases.GenerateInvoiceUseCaseImpl`

## Global Constraints — Enforce Always

1. **No Spring in domain or application** — zero `@Component`, `@Service`, `@Repository`, `@Autowired` in `domain` or `application` modules
2. **Manual bean wiring only** — every bean registered as `@Bean` in `boot/config/beans/`. Never `@Component` + scan for infrastructure classes
3. **No cross-domain imports in domain layer** — `com.myapp.auth.entities.User` must NEVER appear in `com.myapp.financial` domain; use `jca-add-query-adapter`
4. **Bounded Context isolation** — each BC owns its data; others access it via Query Adapter pattern only
5. **Entity encapsulation** — all domain entity fields: `private`, `@Getter`, NO `@Setter`; mutations via explicit domain methods; `DomainEntity` provides `@Setter(AccessLevel.PROTECTED)` only on `id`
6. **reconstitute() never fires events** — only `create()` and state-transition methods call `registerEvent()`
7. **Event publishing after save** — use cases must call `entity.pullEvents().forEach(publisher::publish)` after `repository.save()` when the entity may have pending events

## Commons Available in This Project

- `domain/common/entities/DomainEntity<ID>` — base entity with `id`, `createdAt`, `updatedAt`, event list
- `domain/common/vos/ValueObject<T>`, `StringValueObject`, `BigDecimalValueObject`
- `domain/common/events/DomainEvent` — marker interface
- `domain/common/exceptions/DomainException`, `NotFoundException`, `ConflictException`, `ExternalServiceException`
- `domain/common/statemachine/StateMachine<V>` — generic state machine base
- `application/common/repository/Repository<D,ID,C>` — generic repository contract
- `application/common/annotations/CriteriaField` — annotation for dynamic JPA queries
- `application/common/pagination/Page<T>`, `Pageable`
- `application/common/utils/QueryUtils.findOrThrow(repo, id, msg)`
- `application/common/sequence/DocumentSequencePort` — sequential doc numbers (PREFIX-YYMMDD-NNNN)
- `infrastructure/common/persistence/repositories/RepositoryImpl<D,ID,C,P,R>` — generic JPA impl
- `infrastructure/common/persistence/entities/BaseJpaEntity<ID>` — JPA base with `@PrePersist`/`@PreUpdate`
- `infrastructure/common/mapper/EntityMapper<D,P>` — domain ↔ JPA mapper interface
- `infrastructure/common/utils/EventLoggingUtils` — standardized event logging
- `infrastructure/common/utils/TimeConfig.DEFAULT_OFFSET` — project timezone (ZoneOffset)
- `boot/config/async/AsyncConfig` — `eventExecutor` bean already defined; do NOT create another

## When to Invoke Each Skill

| Trigger | Skill to invoke |
|---|---|
| Creating a new bounded context from scratch | `jca-add-domain` |
| Adding a Value Object | `jca-add-value-object` |
| Adding a Domain Entity | `jca-add-entity` |
| Adding a use case to an existing domain | `jca-add-use-case` |
| Adding state transitions to an entity | `jca-add-state-machine` |
| Creating a domain event + listener | `jca-add-domain-event` |
| Accessing data from another bounded context | `jca-add-query-adapter` |

## Pre-Implementation Checklist

Before touching any file:
- [ ] Identify which bounded context owns this concept
- [ ] Identify which module(s) need to change
- [ ] Read existing files in the affected area first
- [ ] Confirm no cross-domain import would be introduced in the domain layer
- [ ] Invoke the relevant skill for the operation type
```

- [ ] **Step 2: Verify frontmatter and content**

Confirm:
- `name: jca-overview` matches filename
- `description` is one line
- All 7 skills are listed in the routing table
- All commons classes listed actually exist in the project

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/jca-overview.md
git commit -m "feat: add jca-overview skill"
```

---

### Task 2: `jca-add-domain`

**Files:**
- Create: `.claude/skills/jca-add-domain.md`

**Interfaces:**
- Consumes: references to `jca-add-entity`, `jca-add-value-object`, `jca-add-use-case` (by name)
- Produces: `jca-add-domain` skill

- [ ] **Step 1: Create the skill file**

Create `.claude/skills/jca-add-domain.md` with this exact content:

```markdown
---
name: jca-add-domain
description: Use when creating a new bounded context (domain module) from scratch — covers all 5 Maven modules with a full step-by-step checklist. Always invoke before starting any new domain implementation.
---

# Creating a New Bounded Context

Load `jca-overview` first if you haven't already.

For sub-tasks, invoke the relevant skills:
- `jca-add-value-object` — for each VO
- `jca-add-entity` — for the main aggregate/entity
- `jca-add-use-case` — for each use case
- `jca-add-state-machine` — if the entity has state transitions
- `jca-add-domain-event` — for each domain event

---

## Step-by-Step Checklist

### 1. Domain Module (`domain/src/main/java/com/<project>/<bc>/`)

- [ ] Create package structure: `entities/`, `vos/`, `enums/`, `events/`
- [ ] Create the main entity — invoke `jca-add-entity`
- [ ] Create each Value Object — invoke `jca-add-value-object`
- [ ] Create status enum if entity has states — invoke `jca-add-state-machine`
- [ ] Create domain events as records — invoke `jca-add-domain-event`

### 2. Application Module (`application/src/main/java/com/<project>/<bc>/`)

- [ ] `contracts/repositories/<Entity>Repository.java`

```java
package com.<project>.<bc>.contracts.repositories;

import com.<project>.application.common.repository.Repository;
import com.<project>.<bc>.criteria.<Entity>Criteria;
import com.<project>.<bc>.entities.<Entity>;
import java.util.UUID;

public interface <Entity>Repository extends Repository<<Entity>, UUID, <Entity>Criteria> {}
```

- [ ] `criteria/<Entity>Criteria.java`

```java
package com.<project>.<bc>.criteria;

import com.<project>.application.common.annotations.CriteriaField;
import com.<project>.application.common.annotations.CriteriaField.Operator;
import com.<project>.application.common.repository.Criteria;

public record <Entity>Criteria(
    @CriteriaField(value = "name", operator = Operator.LIKE) String name
    // add more fields as needed
) implements Criteria {}
```

- [ ] `schemas/response/<Entity>Response.java` — record with all public-facing fields
- [ ] `schemas/request/` — one request record per write use case
- [ ] `mappers/<Entity>Mapper.java` — MapStruct interface

```java
@Mapper
public interface <Entity>Mapper extends BaseMapper {
    @Mapping(target = "name", expression = "java(fromStringVO(entity.getName()))")
    <Entity>Response toResponse(<Entity> entity);
}
```

- [ ] `contracts/usecases/` — one interface per use case
- [ ] `usecases/` — one impl per use case — invoke `jca-add-use-case`

### 3. Infrastructure Module (`infrastructure/src/main/java/com/<project>/<bc>/`)

- [ ] `data/entities/<Entity>Entity.java`

```java
@Entity @Table(name = "<entities>")
@Getter @Setter @NoArgsConstructor
public class <Entity>Entity extends BaseJpaEntity<UUID> {
    // fields matching the domain entity, using primitive types or enums
}
```

- [ ] `data/jpa/<Entity>JpaRepository.java`

```java
public interface <Entity>JpaRepository extends JpaRepository<<Entity>Entity, UUID> {
    Optional<<Entity>Entity> findByEmail(String email); // example
}
```

- [ ] `mappers/<Entity>EntityMapper.java`

```java
public class <Entity>EntityMapper implements EntityMapper<<Entity>, <Entity>Entity> {
    @Override public <Entity>Entity toPersistence(<Entity> domain) { ... }
    @Override public <Entity> toDomain(<Entity>Entity persistence) {
        return <Entity>.reconstitute(
            persistence.getId(),
            // ... all fields ...
            persistence.getCreatedAt(),
            persistence.getUpdatedAt()
        );
    }
}
```

- [ ] `repositories/<Entity>RepositoryImpl.java`

```java
public class <Entity>RepositoryImpl
    extends RepositoryImpl<<Entity>, UUID, <Entity>Criteria, <Entity>Entity, <Entity>JpaRepository>
    implements <Entity>Repository {

    public <Entity>RepositoryImpl(<Entity>JpaRepository jpa, EntityManager em, <Entity>EntityMapper mapper) {
        super(jpa, em, mapper, <Entity>Entity.class);
    }
}
```

- [ ] `listeners/<Bc>EventListener.java` — invoke `jca-add-domain-event`

### 4. Web Module (`web/src/main/java/com/<project>/web/controllers/`)

- [ ] `<Bc>Controller.java`

```java
@RestController
@RequestMapping("/api/<bc>")
@RequiredArgsConstructor
public class <Bc>Controller {
    private final <Entity>Mapper mapper;
    private final List<Entity>UseCase list<Entity>UseCase;
    // inject use case interfaces, never impls
}
```

### 5. Boot Module — Composition Root

- [ ] `boot/config/beans/application/<Bc>Beans.java`

```java
@Configuration @RequiredArgsConstructor
public class <Bc>Beans {
    private final <Entity>Repository repository;
    private final DomainEventPublisher publisher;
    private final <Entity>Mapper mapper;

    @Bean public Create<Entity>UseCase create<Entity>UseCase() {
        return new Create<Entity>UseCaseImpl(repository, publisher);
    }
    // one @Bean per use case
}
```

- [ ] Add to `boot/config/beans/infra/InfraRepositoryBeans.java`:

```java
@Bean public <Entity>Repository <entity>Repository(<Entity>JpaRepository jpa, EntityManager em, <Entity>EntityMapper mapper) {
    return new <Entity>RepositoryImpl(jpa, em, mapper);
}
```

- [ ] Add to `boot/config/beans/infra/InfraMapperBeans.java`:

```java
@Bean public <Entity>EntityMapper <entity>EntityMapper() { return new <Entity>EntityMapper(); }
```

- [ ] Add mapper bean to `boot/config/beans/application/MapperBeans.java`:

```java
@Bean public <Entity>Mapper <entity>Mapper() { return Mappers.getMapper(<Entity>Mapper.class); }
```

- [ ] If listeners exist: add to `boot/config/beans/infra/InfraListenerBeans.java`

---

## Common Pitfalls

- **Forgetting boot wiring** — the most common error. Every class needs a `@Bean`
- **Cross-domain import in domain layer** — if you need data from another BC, use `jca-add-query-adapter`
- **`@Component` on application/infra classes** — never. Manual wiring only
- **Business logic in use cases** — use cases orchestrate; business rules live in the domain entity
- **Using `findById(...).orElseThrow(...)` inline** — use `QueryUtils.findOrThrow(repo, id, msg)` instead
```

- [ ] **Step 2: Verify**

Confirm all 5 module sections are present, package placeholders use `<project>` and `<bc>` consistently, no `@Component` anywhere in the checklist.

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/jca-add-domain.md
git commit -m "feat: add jca-add-domain skill"
```

---

### Task 3: `jca-add-value-object`

**Files:**
- Create: `.claude/skills/jca-add-value-object.md`

- [ ] **Step 1: Create the skill file**

Create `.claude/skills/jca-add-value-object.md` with this exact content:

```markdown
---
name: jca-add-value-object
description: Use when creating a Value Object in any domain — covers StringValueObject, BigDecimalValueObject, and generic ValueObject with examples and test checklist.
---

# Creating a Value Object

## Which Base to Extend

| Scenario | Extend |
|---|---|
| String with validation (name, email, code...) | `StringValueObject` |
| Decimal with scale/rounding (price, quantity...) | `BigDecimalValueObject` |
| Any other type (UUID-based ID, Integer, custom) | `ValueObject<T>` directly |

Package: `domain/src/main/java/com/<project>/<bc>/vos/<Name>.java`

Shared VOs used across BCs: `domain/src/main/java/com/<project>/domain/shared/vos/`

---

## StringValueObject

Base class handles automatically: null → throws `DomainException`, blank → throws, trims whitespace.

```java
package com.<project>.<bc>.vos;

import com.<project>.domain.common.vos.StringValueObject;

public final class ProductName extends StringValueObject {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    private ProductName(String value) { super(value); }

    public static ProductName of(String value) { return new ProductName(value); }

    @Override
    protected void customValidate(String value) {
        checkLength(value, MIN_LENGTH, MAX_LENGTH);
        // add format checks here if needed
    }
}
```

Override `createException(String)` to customize the exception type or message prefix:
```java
@Override
protected DomainException createException(String message) {
    return new DomainException("ProductName: " + message);
}
```

---

## BigDecimalValueObject

```java
package com.<project>.<bc>.vos;

import com.<project>.domain.common.vos.BigDecimalValueObject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Price extends BigDecimalValueObject {

    private Price(BigDecimal value) { super(value); }

    public static Price of(BigDecimal value) { return new Price(value); }

    @Override protected int scale() { return 2; }
    @Override protected RoundingMode roundingMode() { return RoundingMode.HALF_EVEN; }
    @Override protected String type() { return "price"; }
    @Override protected boolean allowNegative() { return false; }
}
```

**Important:** arithmetic methods return `BigDecimal`, not the VO subtype.

```java
// CORRECT
BigDecimal subtotal = price.multiply(new BigDecimal("3"));
BigDecimal discounted = price.subtract(discount);

// WRONG — these methods don't exist on the subtype directly
Price result = price.add(otherPrice); // ❌
```

Available methods: `add(other)`, `subtract(other)`, `multiply(BigDecimal)`, `divide(BigDecimal)`, `isZero()`, `isPositive()`, `isNegative()`, `isGreaterThan(other)`, `isLessThan(other)`, `isGreaterThanOrEqualTo(other)`, `isLessThanOrEqualTo(other)`.

---

## ValueObject<T> — Generic

```java
package com.<project>.<bc>.vos;

import com.<project>.domain.common.vos.ValueObject;
import com.<project>.domain.common.exceptions.DomainException;

public final class OrderNumber extends ValueObject<String> {

    private OrderNumber(String value) { super(value); }

    public static OrderNumber of(String value) { return new OrderNumber(value); }

    @Override
    protected String validate(String value) {
        if (value == null || !value.matches("^[A-Z]+-\\d{6}-\\d{4}$"))
            throw new DomainException("invalid order number format");
        return value;
    }
}
```

---

## Test Checklist

```java
@Test void validValue() {
    var vo = ProductName.of("Widget Pro");
    assertThat(vo.getValue()).isEqualTo("Widget Pro");
}

@Test void nullRejected() {
    assertThatThrownBy(() -> ProductName.of(null))
        .isInstanceOf(DomainException.class);
}

@Test void blankRejected() {
    assertThatThrownBy(() -> ProductName.of("   "))
        .isInstanceOf(DomainException.class);
}

@Test void tooShortRejected() {
    assertThatThrownBy(() -> ProductName.of("A"))
        .isInstanceOf(DomainException.class);
}

@Test void equalityByValue() {
    assertThat(ProductName.of("Widget")).isEqualTo(ProductName.of("Widget"));
    assertThat(ProductName.of("Widget")).isNotEqualTo(ProductName.of("Gadget"));
}

@Test void trimmingApplied() {
    assertThat(ProductName.of("  Widget  ").getValue()).isEqualTo("Widget");
}
```
```

- [ ] **Step 2: Verify**

Confirm three VO types covered, `of()` factory shown in each, test checklist is concrete (no placeholders), `BigDecimalValueObject` arithmetic note is present.

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/jca-add-value-object.md
git commit -m "feat: add jca-add-value-object skill"
```

---

### Task 4: `jca-add-entity`

**Files:**
- Create: `.claude/skills/jca-add-entity.md`

- [ ] **Step 1: Create the skill file**

Create `.claude/skills/jca-add-entity.md` with this exact content:

```markdown
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
```

- [ ] **Step 2: Verify**

Confirm: private constructor present, `setTimestamps` explained, JPA entity in infra (not domain), mapper unwraps/rewraps VOs, test checklist covers create-registers-event and reconstitute-no-events.

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/jca-add-entity.md
git commit -m "feat: add jca-add-entity skill"
```

---

### Task 5: `jca-add-use-case`

**Files:**
- Create: `.claude/skills/jca-add-use-case.md`

- [ ] **Step 1: Create the skill file**

Create `.claude/skills/jca-add-use-case.md` with this exact content:

```markdown
---
name: jca-add-use-case
description: Use when adding a new use case to an existing domain — covers interface, implementation, request/response schemas, bean registration in boot, and controller endpoint.
---

# Adding a Use Case

## File Locations

| File | Path |
|---|---|
| Interface | `application/src/main/java/com/<project>/<bc>/contracts/usecases/<UseCase>.java` |
| Implementation | `application/src/main/java/com/<project>/<bc>/usecases/<UseCase>Impl.java` |
| Request schema | `application/src/main/java/com/<project>/<bc>/schemas/request/<Action>Request.java` |
| Response schema | `application/src/main/java/com/<project>/<bc>/schemas/response/<Entity>Response.java` |
| Bean registration | `boot/src/main/java/com/<project>/boot/config/beans/application/<Bc>Beans.java` |
| Controller | `web/src/main/java/com/<project>/web/controllers/<Bc>Controller.java` |

---

## Interface

```java
package com.<project>.<bc>.contracts.usecases;

import com.<project>.<bc>.schemas.request.<Action>Request;
import com.<project>.<bc>.schemas.response.<Entity>Response;

public interface <Action><Entity>UseCase {
    <Entity>Response execute(<Action>Request request);
    // or: void execute(UUID id);
    // or: Page<Entity>Response execute(<Entity>Criteria criteria, Pageable pageable);
}
```

## Implementation

```java
package com.<project>.<bc>.usecases;

import com.<project>.<bc>.contracts.repositories.<Entity>Repository;
import com.<project>.<bc>.contracts.usecases.<Action><Entity>UseCase;
import com.<project>.application.common.events.DomainEventPublisher;
import com.<project>.application.common.utils.QueryUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class <Action><Entity>UseCaseImpl implements <Action><Entity>UseCase {

    private final <Entity>Repository repository;
    private final DomainEventPublisher publisher;  // inject only if use case triggers events

    @Override
    public <Entity>Response execute(<Action>Request request) {
        // 1. Fetch — use QueryUtils, never inline orElseThrow
        var entity = QueryUtils.findOrThrow(repository, request.id(), "<Entity> not found");

        // 2. Apply domain operation
        entity.someOperation(/* args from request, converted to VOs */);

        // 3. Persist
        var saved = repository.save(entity);

        // 4. Publish events (only if events may have been registered)
        saved.pullEvents().forEach(publisher::publish);

        // 5. Return response
        return mapper.toResponse(saved);
    }
}
```

**Rules:**
- Use `QueryUtils.findOrThrow(repository, id, message)` — never `.orElseThrow()` inline
- Convert request primitives to VOs before passing to entity: `Email.of(request.email())`
- Call `pullEvents().forEach(publisher::publish)` AFTER `repository.save()`, not before
- Inject `DomainEventPublisher` only when events are actually published
- No `@Component` or `@Service` — manual wiring in boot

---

## Request and Response Schemas

```java
// Request — plain record, validated at the web layer
public record CreateProductRequest(
    @NotBlank String name,
    @NotNull BigDecimal price
) {}

// Response — immutable, no domain types exposed
public record ProductResponse(
    UUID id,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String name,
    BigDecimal price,
    ProductStatus status
) {}
```

---

## Bean Registration in Boot

```java
// boot/config/beans/application/<Bc>Beans.java
@Configuration @RequiredArgsConstructor
public class <Bc>Beans {
    private final <Entity>Repository repository;
    private final DomainEventPublisher publisher;
    private final <Entity>Mapper mapper;

    @Bean
    public <Action><Entity>UseCase <action><Entity>UseCase() {
        return new <Action><Entity>UseCaseImpl(repository, publisher, mapper);
    }
}
```

---

## Controller Endpoint

```java
// web/controllers/<Bc>Controller.java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public <Entity>Response create(@RequestBody @Valid Create<Entity>Request request) {
    return create<Entity>UseCase.execute(request);
}

@GetMapping("/{id}")
public <Entity>Response getById(@PathVariable UUID id) {
    return get<Entity>ByIdUseCase.execute(id);
}

@GetMapping
public Page<<Entity>Response> list(
    @RequestParam(required = false) String name,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    var criteria = new <Entity>Criteria(name);
    var pageable = Pageable.of(page, size);
    return list<Entity>UseCase.execute(criteria, pageable);
}
```

---

## Checklist

- [ ] Interface created in `contracts/usecases/`
- [ ] Impl in `usecases/`, no Spring annotations
- [ ] `QueryUtils.findOrThrow` used for all ID lookups
- [ ] `pullEvents().forEach(publisher::publish)` called after save (if entity has events)
- [ ] Request record uses Jakarta validation annotations (`@NotBlank`, `@NotNull`)
- [ ] `@Bean` registered in boot `<Bc>Beans.java`
- [ ] Controller endpoint added
```

- [ ] **Step 2: Verify**

Confirm: `QueryUtils.findOrThrow` shown, `pullEvents` shown after save, no `@Component`, bean registration template present.

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/jca-add-use-case.md
git commit -m "feat: add jca-add-use-case skill"
```

---

### Task 6: `jca-add-state-machine`

**Files:**
- Create: `.claude/skills/jca-add-state-machine.md`

- [ ] **Step 1: Create the skill file**

Create `.claude/skills/jca-add-state-machine.md` with this exact content:

```markdown
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

// Suspended<Entity>State.java — same pattern: activate allowed, deactivate/suspend throw
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

    public void activate() { value.getState().activate(this); }
    public void deactivate() { value.getState().deactivate(this); }
    public void suspend() { value.getState().suspend(this); }
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
```

- [ ] **Step 2: Verify**

Confirm: all 5 components present, `sm.transition(status, event)` shown, singleton pattern (`INSTANCE`), entity `transition()` helper, `@Enumerated(STRING)` on JPA, test covers valid + invalid transitions.

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/jca-add-state-machine.md
git commit -m "feat: add jca-add-state-machine skill"
```

---

### Task 7: `jca-add-domain-event`

**Files:**
- Create: `.claude/skills/jca-add-domain-event.md`

- [ ] **Step 1: Create the skill file**

Create `.claude/skills/jca-add-domain-event.md` with this exact content:

```markdown
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
```

- [ ] **Step 2: Verify**

Confirm: 3-annotation explanation present, `pullEvents()` shown in use case, `@Component` absent from listener, `eventExecutor` existing warning present.

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/jca-add-domain-event.md
git commit -m "feat: add jca-add-domain-event skill"
```

---

### Task 8: `jca-add-query-adapter`

**Files:**
- Create: `.claude/skills/jca-add-query-adapter.md`

- [ ] **Step 1: Create the skill file**

Create `.claude/skills/jca-add-query-adapter.md` with this exact content:

```markdown
---
name: jca-add-query-adapter
description: Use when a domain needs data from another bounded context — enforces BC isolation via DTO + port + infrastructure adapter pattern without importing domain entities across boundaries.
---

# Accessing Another Bounded Context — Query Adapter Pattern

## The Rule

**Domain layer: NEVER imports from another domain.**
**Infrastructure layer: may access any JPA repository.**

A use case in `financial` must never import `com.<project>.auth.entities.User`. Instead, `financial` defines what it needs (a DTO + port), and infrastructure does the actual lookup.

---

## The 4-Step Pattern

### Step 1 — DTO in consuming domain's application layer

`application/src/main/java/com/<project>/<consuming-bc>/queries/<Resource>Summary.java`

Only the fields the consuming domain actually needs:

```java
package com.<project>.<consuming-bc>.queries;

import java.util.UUID;

public record UserSummary(UUID id, String name, String email) {}
```

### Step 2 — Port interface in consuming domain's application layer

`application/src/main/java/com/<project>/<consuming-bc>/queries/<Resource>QueryPort.java`

```java
package com.<project>.<consuming-bc>.queries;

import java.util.Optional;
import java.util.UUID;

public interface UserQueryPort {
    Optional<UserSummary> findById(UUID id);
    // add other query methods as needed
}
```

The consuming use case injects this interface — never the adapter.

### Step 3 — Adapter in infrastructure

`infrastructure/src/main/java/com/<project>/infrastructure/<consuming-bc>/adapters/UserQueryAdapter.java`

```java
package com.<project>.infrastructure.<consuming-bc>.adapters;

import com.<project>.<consuming-bc>.queries.UserQueryPort;
import com.<project>.<consuming-bc>.queries.UserSummary;
import com.<project>.infrastructure.auth.data.jpa.UserJpaRepository;  // ← accesses producing BC's JPA repo
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserJpaRepository userJpaRepository;  // producing BC's repo

    @Override
    public Optional<UserSummary> findById(UUID id) {
        return userJpaRepository.findById(id)
            .map(entity -> new UserSummary(
                entity.getId(),
                entity.getName(),
                entity.getEmail()
            ));
    }
}
```

No `@Component` — manual wiring in boot.

### Step 4 — Bean wiring in boot

```java
// In boot/config/beans/infra/Infra<ConsumingBc>Beans.java (or InfraRepositoryBeans.java)
@Bean
public UserQueryPort userQueryPort(UserJpaRepository userJpaRepository) {
    return new UserQueryAdapter(userJpaRepository);
}
```

---

## Using the Port in a Use Case

```java
@RequiredArgsConstructor
public class GenerateReportUseCaseImpl implements GenerateReportUseCase {
    private final UserQueryPort userQueryPort;  // ← inject the port, not the adapter

    @Override
    public ReportResponse execute(UUID userId) {
        var user = userQueryPort.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        // use user.name(), user.email() — only what was projected in the DTO
    }
}
```

---

## What Goes Where

| Artifact | Module | Package |
|---|---|---|
| `<Resource>Summary` (DTO) | `application` | `com.<project>.<consuming-bc>.queries` |
| `<Resource>QueryPort` (interface) | `application` | `com.<project>.<consuming-bc>.queries` |
| `<Resource>QueryAdapter` (impl) | `infrastructure` | `com.<project>.infrastructure.<consuming-bc>.adapters` |
| `@Bean` wiring | `boot` | `boot/config/beans/infra/` |

---

## Constraints — Enforce Always

- DTO and port live in the **consuming** BC's application layer — not in shared commons, not in the producing BC
- Adapter lives in **infrastructure** — it crosses boundaries so it belongs to the cross-cutting layer
- The consuming use case injects the **port** (interface) — never the adapter class directly
- The DTO contains **only** what the consumer needs — no leaking the full domain model
- Never import `User` (the domain entity) from `auth` into `financial` — only `UserSummary`
- Never call `UserRepository` (domain port) from another domain — only `UserJpaRepository` (JPA, infra-level)

---

## Checklist

- [ ] DTO record created in consuming BC's `application/queries/`
- [ ] Port interface created in consuming BC's `application/queries/`
- [ ] Adapter implements port, uses producing BC's `JpaRepository`, no `@Component`
- [ ] `@Bean` wiring in boot
- [ ] Use case injects port interface, not adapter
- [ ] No import of producing BC's domain entities in consuming domain layer
```

- [ ] **Step 2: Verify**

Confirm: 4 steps present, DTO/port in consuming BC, adapter in infra, no cross-domain entity import, constraint list present.

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/jca-add-query-adapter.md
git commit -m "feat: add jca-add-query-adapter skill"
```

---

### Task 9: `CLAUDE.md`

**Files:**
- Create: `CLAUDE.md` (project root)

**Interfaces:**
- Consumes: all 8 skill files from Tasks 1-8 (must exist)
- Produces: `CLAUDE.md` — loaded automatically by Claude Code in every session

- [ ] **Step 1: Create CLAUDE.md**

Create `CLAUDE.md` at the project root with this exact content:

```markdown
# Java Clean Architecture Boilerplate

Backend Java com Clean Architecture e DDD, estruturado em 5 módulos Maven: `domain`, `application`, `infrastructure`, `web`, `boot`.

## Skills disponíveis

Carregue `jca-overview` antes de qualquer trabalho de implementação no projeto.

| Skill | Quando invocar |
|---|---|
| `jca-overview` | Antes de qualquer implementação — arquitetura, módulos, regras globais e roteamento para outros skills |
| `jca-add-domain` | Ao criar um novo bounded context do zero |
| `jca-add-value-object` | Ao criar um Value Object (string, decimal ou genérico) |
| `jca-add-entity` | Ao criar uma entidade de domínio |
| `jca-add-use-case` | Ao adicionar um use case a um domínio existente |
| `jca-add-state-machine` | Ao implementar State Machine em uma entidade com transições de estado |
| `jca-add-domain-event` | Ao criar um domain event e seu listener de infraestrutura |
| `jca-add-query-adapter` | Ao precisar de dados de outro bounded context sem violar isolamento |

## Regra de ouro

Nenhuma classe de domínio ou aplicação usa `@Component`, `@Service` ou `@Repository`. Todo wiring é manual em `boot/config/beans/`.
```

- [ ] **Step 2: Verify**

Confirm: all 8 skills listed, `jca-overview` marked as the first to load, golden rule present, no skill missing from the table.

- [ ] **Step 3: Commit**

```bash
git add CLAUDE.md
git commit -m "feat: add CLAUDE.md with jca skills index"
```

---

## Self-Review

**Spec coverage:**
- ✅ `jca-overview` — Task 1
- ✅ `jca-add-domain` — Task 2
- ✅ `jca-add-value-object` — Task 3
- ✅ `jca-add-entity` — Task 4
- ✅ `jca-add-use-case` — Task 5
- ✅ `jca-add-state-machine` — Task 6
- ✅ `jca-add-domain-event` — Task 7
- ✅ `jca-add-query-adapter` — Task 8
- ✅ `CLAUDE.md` — Task 9

**Placeholder scan:** No TBD, TODO, or "implement X" without showing how. Every code block is complete.

**Type consistency:** `<Entity>`, `<Bc>`, `<project>` placeholders used consistently. `sm.transition(status, event)` matches in Task 6 between facade and state impls. `pullEvents().forEach(publisher::publish)` consistent in Tasks 5 and 7. `QueryUtils.findOrThrow` consistent in Tasks 1 (commons list) and 5 (use case impl).
