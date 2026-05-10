# Domain Module

The domain is the heart of the application. It contains the business rules, and it has zero dependencies on frameworks, databases, or any external library beyond what is strictly necessary for the language itself.

If the domain compiles without Spring, JPA, or any infrastructure concern — it is correct.

---

## Structure

```
domain/
└── com.boilerplate.domain/
    ├── auth/
    │   ├── entities/       User
    │   ├── enums/          UserRole
    │   ├── events/         UserCreatedEvent
    │   └── vos/            FullName, Password
    ├── common/
    │   ├── entities/       DomainEntity<ID>
    │   ├── events/         DomainEvent
    │   ├── exceptions/     DomainException, NotFoundException, ConflictException
    │   └── vos/            ValueObject<T>, StringValueObject
    └── shared/
        └── vos/            Email
```

---

## Base Abstractions

### `ValueObject<T>`

All value objects extend this class. It enforces immutability and value-based equality.

```java
public abstract class ValueObject<T> {
    protected final T value;

    protected ValueObject(T value) {
        this.value = validate(value);
    }

    protected abstract T validate(T value);
}
```

Validation runs at construction time — an invalid value object cannot exist.

### `StringValueObject`

Extends `ValueObject<String>` with common string validations: null check, blank check, and a `checkLength(min, max)` helper. Subclasses implement `customValidate` for domain-specific rules.

### `DomainEntity<ID>`

Base class for all entities. Carries `id`, `createdAt`, `updatedAt`, and a list of pending domain events.

```java
protected void registerEvent(DomainEvent event) { ... }
public List<DomainEvent> pullEvents() { ... }  // clears after reading
```

`pullEvents()` is destructive by design — events are meant to be consumed once.

---

## Value Objects

### `Email` _(shared)_

Lives in `domain/shared` because email is not exclusive to the auth context — any future domain module can use it.

Validates format via regex, normalizes to lowercase, and enforces a max length of 254 characters (the RFC 5321 limit).

### `FullName`

Validates minimum length of 2 and maximum of 100 characters.

### `Password`

Has two factory methods that carry semantic meaning:

```java
Password.fromRaw("plaintext")     // validates complexity (8–128 chars)
Password.fromHashed("$2a$10$...") // skips complexity validation
```

A `isHashed()` flag makes the state explicit at all times. The entity guard `checkIfPasswordIsHashed` ensures no plain-text password ever reaches `User`.

---

## The `User` Entity

`User` is the aggregate root of the `auth` context.

It exposes only two factory methods:

- `User.create(...)` — generates a new UUID, sets defaults, registers a `UserCreatedEvent`
- `User.reconstitute(...)` — rebuilds from persistence, registers no events

State-changing methods are intentionally narrow:

```java
user.rename(FullName)
user.changeEmail(Email)
user.changePassword(Password)   // guards against unhashed passwords
user.toggleActive()
user.promote()
user.demote()
```

No setters are exposed. The entity controls its own invariants.

---

## Domain Events

`UserCreatedEvent` carries only what is necessary — `userId` and `email`. It is registered automatically on `User.create()` and published by the application layer after saving.

To add new events, implement `DomainEvent` and call `registerEvent(...)` inside the entity method that triggers the state change.

---

## Exceptions

| Exception | HTTP mapping | When to throw |
|---|---|---|
| `DomainException` | 422 | Business rule violation |
| `NotFoundException` | 404 | Entity not found |
| `ConflictException` | 409 | Duplicate or conflicting state |

All exceptions are unchecked. The `RestExceptionHandler` in `web` maps them to `ProblemDetail` responses automatically.

---

## What to keep

- The base abstractions (`ValueObject`, `StringValueObject`, `DomainEntity`) — they are the foundation of everything else.
- The event system — even if you don't use events now, the infrastructure is already there.
- The exception hierarchy — adding new exception types here is free and encouraged.

## What to extend

- Add new value objects to `shared/vos` for types reused across contexts (e.g., `PhoneNumber`, `CPF`, `Money`).
- Add new aggregate roots following the same `create` / `reconstitute` pattern.
- Add new domain events for any state change that other parts of the system should react to.

## What you can change

- `UserRole` — add or rename roles freely. Just update `getAuthorities()` in `UserEntity` and the `@AdminOnly`-style annotations accordingly.
- Validation rules inside value objects — they are self-contained and do not affect anything outside the VO.
