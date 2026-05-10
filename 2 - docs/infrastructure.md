# Infrastructure Module

The infrastructure module implements everything the application declared it needs. JPA entities, repositories, security adapters, JWT, Flyway migrations — all the details that connect the pure business logic to the real world.

Nothing in this module leaks into `domain` or `application`. The dependency arrow points inward.

---

## Structure

```
infrastructure/
└── com.boilerplate.infrastructure/
    ├── auth/
    │   ├── adapters/       AuthenticationAdapter, PasswordEncoderAdapter, TokenProviderAdapter
    │   ├── data/
    │   │   ├── entities/   UserEntity
    │   │   └── jpa/        UserJpaRepository
    │   ├── mappers/        UserEntityMapper
    │   ├── repositories/   UserRepositoryImpl
    │   └── security/       AuthorizationService, TokenService
    └── common/
        ├── mapper/         EntityMapper<D, P>
        ├── persistence/
        │   ├── entities/   BaseJpaEntity<ID>, PersistenceEntity<ID>
        │   └── repositories/ RepositoryImpl, CriteriaUtils, PageableAdapter
        ├── publishers/     DomainEventPublisherImpl
        └── utils/          TimeConfig
```

---

## JPA Entities

JPA entities are completely separate from domain entities. They are persistence concerns and carry JPA annotations. The domain never sees them.

### `BaseJpaEntity<ID>`

```java
@MappedSuperclass
public abstract class BaseJpaEntity<ID> implements PersistenceEntity<ID> {
    @Id private ID id;
    @Column(nullable = false, updatable = false) private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist protected void prePersist() { ... }
    @PreUpdate  protected void preUpdate()  { ... }
}
```

`createdAt` is set on first persist and never updated. `updatedAt` is set on every update. Both use `TimeConfig.DEFAULT_OFFSET` for timezone consistency.

### `UserEntity`

Extends `BaseJpaEntity<UUID>` and implements `UserDetails` — making it directly usable by Spring Security without any adapter.

`getAuthorities()` is role-aware: `ADMIN` receives both `ROLE_ADMIN` and `ROLE_USER`, so admin users always have user-level access as well.

---

## The Generic Repository

`RepositoryImpl<D, ID, C, P, R>` is the core infrastructure pattern of this boilerplate. It implements the full `Repository<D, ID, C>` contract generically:

```java
public class RepositoryImpl<
    D extends DomainEntity<ID>, ID, C extends Criteria,
    P extends PersistenceEntity<ID>, R extends JpaRepository<P, ID>>
        implements Repository<D, ID, C> { ... }
```

Five type parameters, one implementation. Every repository in the system extends this and gets all standard operations for free:

- `save`, `saveBatch`
- `delete`, `deleteAll`
- `findById`, `existsById`
- `count`, `count(criteria)`
- `findAll`, `findAll(pageable)`, `findAll(criteria, pageable)`

Specific repositories only need to implement their domain-specific queries:

```java
public class UserRepositoryImpl
        extends RepositoryImpl<User, UUID, UserCriteria, UserEntity, UserJpaRepository>
        implements UserRepository {

    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }
}
```

---

## The Criteria Engine

`CriteriaUtils.buildPredicates(criteria, root, cb)` is the engine behind dynamic filtering.

It reads the record components of any `Criteria` implementation via reflection, checks for `@CriteriaField` annotations, skips null values, and builds JPA `Predicate` objects for each active filter.

```java
for (RecordComponent component : criteria.getClass().getRecordComponents()) {
    var annotation = component.getAnnotation(CriteriaField.class);
    if (annotation == null) continue;

    Object value = component.getAccessor().invoke(criteria);
    if (value == null) continue;

    Path<Object> path = resolvePath(root, annotation.value());
    predicates.add(buildPredicate(cb, path, value, annotation.operator()));
}
```

Dot notation (`"address.city"`) is resolved by `resolvePath`, which splits on `.` and traverses the JPA path accordingly.

The result is plugged directly into the JPA criteria query — no string concatenation, no HQL injection risks, fully type-safe.

---

## Adapters

### `AuthenticationAdapter`

Delegates to Spring Security's `AuthenticationManager`. If authentication succeeds, casts `authentication.getPrincipal()` to `UserEntity` and maps it to the domain `User` — avoiding a redundant database query.

### `PasswordEncoderAdapter`

Wraps `BCryptPasswordEncoder`. Implements `PasswordEncoderPort`. Swap the implementation here to change the hashing algorithm globally.

### `TokenProviderAdapter`

Wraps `TokenService`. Implements `TokenProviderPort`. Translates between the application's domain `User` and the JWT infrastructure.

---

## Token Service

`TokenService` handles JWT creation and validation using `auth0/java-jwt`.

- Access tokens carry `subject` (email) and `role` claim
- Refresh tokens carry a `refresh: true` claim to prevent them from being used as access tokens
- `validateToken` returns the subject or `null` — never throws, safe to call in filters
- Expiration times are configurable via `application.properties`

---

## Flyway

Migrations live in `infrastructure/src/main/resources/db/migration/` following the naming convention `V{version}__{description}.sql`.

The initial migration creates the `users` table with:
- UUID primary key
- Unique constraint on `email`
- Check constraint on `role` values
- Index on `email` for fast lookup
- `created_at` / `updated_at` with timezone support

---

## Domain Event Publisher

`DomainEventPublisherImpl` wraps Spring's `ApplicationEventPublisher`. After saving an entity, the application layer calls `publisher.publish(user.pullEvents())` to dispatch domain events to any registered `@EventListener`.

---

## What to keep

- `RepositoryImpl` and `CriteriaUtils` — the generic repository system is the core infrastructure pattern.
- `BaseJpaEntity` — all new JPA entities should extend this.
- `EntityMapper<D, P>` — always bridge domain and persistence through a dedicated mapper.

## What to extend

- Add new JPA entities in `auth/data/entities/` (or in a new bounded context folder).
- Add new Flyway migrations for schema changes — never modify existing migration files.
- Add new adapters in `auth/adapters/` (or context-specific folders) for new ports declared in `application`.

## What you can change

- The JWT library — replace `auth0/java-jwt` with Nimbus or any other. Only `TokenService` changes.
- The hashing algorithm — replace BCrypt in `PasswordEncoderAdapter`. Nothing else changes.
- The database — Flyway and JPA support multiple databases. Changing from PostgreSQL requires updating the driver, dialect, and migration SQL syntax.
