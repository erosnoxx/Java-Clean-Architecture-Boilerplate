# JCA Skills Design

**Date:** 2026-07-03  
**Prefix:** `jca` (Java Clean Architecture)  
**Location:** `.claude/skills/`  
**Purpose:** Project-scoped Claude skills that encode architectural conventions for any project derived from this boilerplate.

---

## Context

This boilerplate enforces Clean Architecture with DDD patterns across 5 Maven modules: `domain`, `application`, `infrastructure`, `web`, `boot`. Skills document how to extend the architecture correctly — they are invoked by Claude during implementation, not read by humans as docs.

---

## Skill Inventory

### 1. `jca-overview`
**Entry point.** Loaded before any backend work.

Content:
- Module responsibilities (domain → application → infrastructure → web → boot)
- Global constraints: no Spring in domain/application; no cross-domain imports in domain; all beans wired manually in boot
- Package naming convention: `com.<project>.<domain>.*`
- When to invoke each other skill
- Checklist Claude must run before touching any file: read relevant existing code first, check which BC owns the concept, confirm which module the change belongs to

Trigger: whenever Claude opens any `.java` file in the project, or when implementing any backend feature.

---

### 2. `jca-add-domain`
**Create a new Bounded Context from scratch.**

Content:
- Step-by-step checklist: domain entity → VOs → repository contract → use cases → JPA entity → mapper → repository impl → controller → bean wiring
- Package structure per module for the new domain
- Common pitfalls: forgetting to register beans in boot, importing from another domain in the domain layer
- References: invoke `jca-add-entity`, `jca-add-value-object`, `jca-add-use-case` for sub-tasks

---

### 3. `jca-add-value-object`
**Create a Value Object.**

Content:
- Three VO types and when to use each:
  - `StringValueObject` — extend, override `customValidate(String)` for extra rules, use `checkLength(min, max)`, use `createException(msg)` for custom error type
  - `BigDecimalValueObject` — extend, implement `scale()`, `roundingMode()`, `type()`, `allowNegative()`; arithmetic returns `BigDecimal`, not the VO
  - `ValueObject<T>` — extend directly for any other type, implement `validate(T)`
- Factory method convention: `public static MyVO of(T value)`
- Package: `domain/<bc>/vos/`
- Test checklist: valid value, null rejection, blank/invalid rejection, equality by value

---

### 4. `jca-add-entity`
**Create a Domain Entity.**

Content:
- Mandatory pattern: `private` constructor + `static create(...)` + `static reconstitute(...)`
- `create()`: validates domain rules, sets initial state, calls `registerEvent()` if needed
- `reconstitute()`: pure restore — no validation, no events, calls `setTimestamps(createdAt, updatedAt)`, accepts all fields including timestamps
- `@Getter` on class, NO `@Setter` — mutations via explicit domain methods only
- `setId(UUID)` available because `DomainEntity` has `@Setter(AccessLevel.PROTECTED)` on `id`
- Package: `domain/<bc>/entities/`
- JPA counterpart goes in `infrastructure/<bc>/data/entities/` — never share the same class
- Test checklist: create registers events, reconstitute does NOT register events, invalid args throw DomainException

---

### 5. `jca-add-use-case`
**Add a Use Case to an existing domain.**

Content:
- Interface in `application/<bc>/contracts/usecases/`
- Implementation in `application/<bc>/usecases/`
- Request record in `application/<bc>/schemas/request/` (if needed)
- Response record in `application/<bc>/schemas/response/` (if needed)
- Use `QueryUtils.findOrThrow(repository, id, "message")` instead of inline `.orElseThrow()`
- After `repository.save()`: call `entity.pullEvents().forEach(publisher::publish)`
- `DomainEventPublisher` must be injected if events are published
- Bean registration: `@Bean` in `boot/config/beans/application/<Bc>Beans.java`
- Controller endpoint in `web/controllers/<Bc>Controller.java`
- No `@Component`, `@Service`, `@Repository` on use case classes — manual wiring only

---

### 6. `jca-add-state-machine`
**Implement State Machine on an existing Entity.**

Content:
- Full pattern with 5 components:
  1. **Status enum** — constants with abstract `getState()` returning singleton state impl
  2. **State interface** — one method per allowed transition (e.g., `void activate(EntityStateMachine sm)`)
  3. **State implementations** — one per status, singleton (`INSTANCE`), `private` constructor; valid transitions call `sm.transition(newStatus, event)`; invalid transitions throw `DomainException`
  4. **StateMachine facade** — extends `StateMachine<StatusEnum>`, created via `of(entity)`, exposes `transition(Status, DomainEvent)`, one public method per transition that delegates to state
  5. **Entity integration** — private `transition(Consumer<StateMachine>)` helper; public `activate()`, `deactivate()` etc. call it; sets `this.status` and calls `registerEvent()`
- Packages:
  - Enum: `domain/<bc>/enums/`
  - State interface + impls: `domain/<bc>/states/`
  - StateMachine: `domain/<bc>/statemachine/`
- Convenience: keep `isActive()` → `status == ACTIVE` if applicable
- Events: one per meaningful transition, defined as records in `domain/<bc>/events/`
- JPA: `@Enumerated(EnumType.STRING)` on status column in entity
- Test checklist: each valid transition, each invalid transition throws DomainException, events registered correctly

---

### 7. `jca-add-domain-event`
**Create a Domain Event and its listener.**

Content:
- Event: `record MyEvent(UUID entityId, ...) implements DomainEvent {}` in `domain/<bc>/events/`
- Register in entity: `registerEvent(new MyEvent(...))` inside `create()` or transition methods
- Publish in use case: after `repository.save()`, call `entity.pullEvents().forEach(publisher::publish)`
- Listener in `infrastructure/<bc>/listeners/<Bc>EventListener.java`:
  ```
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Async("eventExecutor")
  @Transactional(propagation = REQUIRES_NEW)
  public void on(MyEvent event) { ... }
  ```
- Use `EventLoggingUtils.logEventStart(event, details)` and `logEventEnd(event)` in every listener method
- No `@Component` on listener — register as `@Bean` in `boot/config/beans/infra/InfraListenerBeans.java`
- `AsyncConfig` with `eventExecutor` bean already exists in boot — do not create a second one

---

### 8. `jca-add-query-adapter`
**Access another Bounded Context's data without violating BC boundaries.**

Rule: domain layer NEVER imports from another domain. Infrastructure may access any data.

Content (with example: domain `financial` needs user data from domain `auth`):

Step 1 — DTO in consuming domain's application layer:
- `application/<consuming-bc>/queries/<Resource>Summary.java` — record with only the fields needed
- Example: `record UserSummary(UUID id, String name, String email) {}`

Step 2 — Port in consuming domain's application layer:
- `application/<consuming-bc>/queries/<Resource>QueryPort.java` — interface
- Example: `interface UserQueryPort { Optional<UserSummary> findById(UUID id); }`

Step 3 — Adapter in infrastructure:
- `infrastructure/<consuming-bc>/adapters/<Resource>QueryAdapter.java`
- Implements the port; uses the producing domain's JPA repository directly
- Maps producing domain's JPA entity to the DTO
- No `@Component` — manual wiring

Step 4 — Bean wiring in boot:
- Register adapter as `@Bean` implementing the port interface
- Inject the producing domain's `JpaRepository`

Constraints:
- DTO and port live in the CONSUMING domain's application layer
- Adapter lives in infrastructure (neutral ground)
- Consuming domain's use case injects the port — never the adapter directly
- Never import `User` (domain entity) from auth into financial — only the DTO

---

## CLAUDE.md

```markdown
# Java Clean Architecture Boilerplate

Backend Java com Clean Architecture, DDD e 5 módulos Maven: domain, application, infrastructure, web, boot.

## Skills disponíveis

Sempre carregue `jca-overview` antes de qualquer trabalho no projeto.

| Skill | Quando invocar |
|---|---|
| `jca-overview` | Antes de qualquer implementação — arquitetura, módulos, regras globais |
| `jca-add-domain` | Ao criar um novo bounded context do zero |
| `jca-add-value-object` | Ao criar um Value Object |
| `jca-add-entity` | Ao criar uma entidade de domínio |
| `jca-add-use-case` | Ao adicionar um use case a um domínio existente |
| `jca-add-state-machine` | Ao implementar State Machine em uma entidade |
| `jca-add-domain-event` | Ao criar um domain event e seu listener |
| `jca-add-query-adapter` | Ao precisar de dados de outro bounded context |
```

---

## File Structure

```
.claude/
  settings.json
  skills/
    jca-overview.md
    jca-add-domain.md
    jca-add-value-object.md
    jca-add-entity.md
    jca-add-use-case.md
    jca-add-state-machine.md
    jca-add-domain-event.md
    jca-add-query-adapter.md
CLAUDE.md
```

---

## Cross-Skill References

- `jca-add-domain` references `jca-add-entity`, `jca-add-value-object`, `jca-add-use-case`
- `jca-add-entity` references `jca-add-state-machine` (if entity needs states), `jca-add-domain-event`
- `jca-add-use-case` references `jca-add-domain-event` (for event publishing)
- `jca-overview` references all others as the navigation hub
