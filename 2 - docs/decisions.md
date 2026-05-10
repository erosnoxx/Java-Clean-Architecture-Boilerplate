# Architectural Decisions

This document records the key decisions made during the design of this boilerplate — not just what was chosen, but why, and what the trade-offs are.

If you are working on a project derived from this boilerplate and something feels unnecessarily complex, read this first. The complexity is likely intentional.

---

## ADR-001 — Manual Bean Wiring

**Decision:** Classes in `infrastructure` and `application` carry no `@Service`, `@Repository`, or `@Component` annotations. All beans are instantiated explicitly in `@Configuration` classes inside `boot`.

**Why:** In a typical Spring application, `@Component` annotations scatter wiring decisions across the codebase. You find out what depends on what by reading each class individually. Manual wiring in `boot` makes the entire dependency graph visible in one place — auditable, explicit, and refactorable without searching.

It also enforces the architectural constraint: if a bean in `application` tries to import something from `infrastructure`, it will fail to compile — because `application` does not depend on `infrastructure`. The wiring is the only place where those two worlds meet, and that place is `boot`.

**Trade-off:** More boilerplate in `boot` when adding new classes. Every new use case or adapter requires a `@Bean` method. This is a worthwhile trade for a codebase that values legibility over convenience.

---

## ADR-002 — The Generic Repository and Criteria System

**Decision:** A single generic `RepositoryImpl<D, ID, C, P, R>` implements the full repository contract. Dynamic filtering is handled by `CriteriaUtils`, which reads `@CriteriaField` annotations from `Criteria` records at runtime via reflection.

**Why:** The alternative is writing a custom repository for every entity with custom JPQL or Specification classes. This creates duplication and makes adding a new filter a multi-file change.

With the Criteria system, adding a new filter to a query is a one-line change: add a field to the criteria record with `@CriteriaField`. No new query method, no new Specification, no SQL string.

The generic repository means that `save`, `findById`, `count`, `findAll`, and pagination work identically for every entity in the system. Only domain-specific queries (like `findByEmail`) require any additional code.

**Trade-off:** Reflection has a runtime cost and reduces compile-time safety for field names. The `value` in `@CriteriaField("name")` is a string — a typo will only surface at runtime. This is acceptable for the ergonomic gain, but should be considered when performance is critical.

---

## ADR-003 — Password VO with `fromRaw` / `fromHashed`

**Decision:** `Password` has two factory methods — `fromRaw` (validates complexity) and `fromHashed` (skips validation). An `isHashed` flag makes the state explicit. The entity guards against unhashed passwords at every entry point.

**Why:** The alternative is two separate classes (`RawPassword` and `HashedPassword`) or a convention that callers must remember. Two classes is cleaner in theory but verbose in practice — every method signature that touches passwords needs to distinguish between them, and the mapper needs to know which to use when reconstructing from the database.

The single `Password` VO with a flag keeps the API surface small while making the state impossible to misread. `isHashed()` is always queryable. The entity's `checkIfPasswordIsHashed` guard means the wrong state will throw at construction time — not silently persist a plain-text password.

**Trade-off:** The `isHashed` flag adds a small amount of state to an otherwise pure value object. It could be argued that this violates value object purity. The practical safety guarantee outweighs the theoretical concern.

---

## ADR-004 — `PasswordEncoderPort` in Application

**Decision:** Hashing is declared as a port in `application` and implemented in `infrastructure`. The domain never sees a hashing library.

**Why:** Password hashing is an infrastructure concern. BCrypt, Argon2, SCrypt — these are implementation details. If the domain knew about BCrypt, swapping algorithms would require changing business logic. With a port, swapping algorithms is a one-class change in `infrastructure`.

It also makes use cases trivially testable — the port can be mocked, and the test does not need to configure a real password encoder.

---

## ADR-005 — `UserEntity` implements `UserDetails`

**Decision:** The JPA entity `UserEntity` implements Spring Security's `UserDetails` directly.

**Why:** The alternative is a separate `UserDetailsAdapter` that wraps `UserEntity`. This adds a class whose only job is to delegate every method to the entity it wraps — pure noise.

`UserEntity` is already an infrastructure class. It lives in `infrastructure`, it depends on JPA and Spring Security, and it is never exposed to the domain. Having it implement `UserDetails` is not a layer violation — it is a pragmatic consolidation of two infrastructure responsibilities into one class.

**Trade-off:** `UserEntity` now has two roles — persistence model and security principal. If Spring Security's `UserDetails` interface ever conflicts with JPA mapping needs, this could become a problem. In practice, this has never been an issue for standard CRUD applications.

---

## ADR-006 — Seeder System with JSON Files

**Decision:** Initial data is defined in JSON files under `resources/seeders/`. A generic `Seeder<T>` interface and `DatabaseSeeder` orchestrator handle deserialization and execution.

**Why:** Hard-coded seeders are fragile — changing the initial data requires recompiling. Environment-variable-driven seeders are operational noise. JSON files are version-controlled, readable, and editable without touching code.

The generic `Seeder<T>` interface means the seeder infrastructure is completely reusable. Adding a new seeder for a new domain requires only a JSON file and a class — `DatabaseSeeder` picks it up automatically.

All seeders are idempotent. Running the application twice does not duplicate data.

**Trade-off:** JSON field names must match the `record` component names exactly. A mismatch fails silently (Jackson ignores unknown fields by default) or throws at deserialization. This is a known risk and acceptable given the simplicity of the seed data.

---

## ADR-007 — Annotation-Based Authorization

**Decision:** Route authorization uses custom annotations (`@AdminOnly`, `@AuthenticatedOnly`) backed by `@PreAuthorize`, rather than configuring route patterns in `SecurityConfiguration`.

**Why:** Pattern-based authorization in `SecurityConfiguration` couples security rules to URL structure. When a route is renamed or moved, the security rule must be updated separately — and the connection is invisible unless you know to look.

Method-level annotations are co-located with the routes they protect. Reading a controller method tells you immediately who can call it. The rule and the route move together.

**Trade-off:** Security rules are scattered across controllers rather than centralized in one place. For teams that prefer a security-first audit approach, a centralized `SecurityMatchers` is easier to review. Both approaches can coexist — public endpoints are still declared centrally in `SecurityMatchers`.

---

## ADR-008 — Separate JPA Entity and Domain Entity

**Decision:** `UserEntity` (JPA) and `User` (domain) are separate classes, bridged by `UserEntityMapper`.

**Why:** Collapsing them into one class means the domain entity carries JPA annotations, lifecycle callbacks, and persistence concerns. It also means the entity's constructor must satisfy both JPA (no-arg constructor required) and domain invariants (all fields required at creation). These two constraints are fundamentally incompatible.

Separate classes mean each can be designed for its own purpose. The domain entity enforces invariants. The JPA entity handles persistence mechanics. The mapper is explicit about the translation.

**Trade-off:** More classes and a mapping step on every database operation. For simple CRUD, this can feel like overhead. For any system with real business logic, it pays for itself quickly.

---

## ADR-009 — `Email` in `domain/shared`

**Decision:** `Email` lives in `domain/shared/vos` rather than `domain/auth/vos`.

**Why:** Email is not a concept exclusive to authentication. Any future domain module — customers, orders, notifications — might need an email value object with the same validation rules. Placing it in `auth` would force other modules to depend on an unrelated context to reuse it.

`shared` is the designated home for domain primitives that belong to the language of the business, not to any specific bounded context.

**Trade-off:** `shared` can become a dumping ground if not managed carefully. The rule is strict: only value objects that are genuinely reusable across multiple bounded contexts belong in `shared`. Everything else belongs in its own context.
