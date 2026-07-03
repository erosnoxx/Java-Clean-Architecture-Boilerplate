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
