# Application Module

The application layer orchestrates the domain. It defines what the system can do — use cases — and declares what it needs from the outside world — ports. It never implements infrastructure concerns itself.

The rule is simple: if a class in this module imports anything from `infrastructure` or `web`, something is wrong.

---

## Structure

```
application/
└── com.boilerplate.application/
    ├── auth/
    │   ├── contracts/
    │   │   ├── ports/          AuthenticationPort, PasswordEncoderPort, TokenProviderPort
    │   │   ├── repositories/   UserRepository
    │   │   └── usecases/
    │   │       ├── auth/       LoginUseCase, RegisterUseCase, RefreshTokenUseCase
    │   │       └── users/      GetUserByIdUseCase, GetUserByEmailUseCase, ListUsersUseCase,
    │   │                       UpdateUserUseCase, ChangePasswordUseCase,
    │   │                       ToggleUserUseCase, PromoteUserUseCase, DemoteUserUseCase
    │   ├── criteria/           UserCriteria
    │   ├── mappers/            UserMapper
    │   ├── schemas/
    │   │   ├── request/        LoginRequest, RegisterRequest, RefreshTokenRequest,
    │   │   │                   UpdateUserRequest, ChangePasswordRequest
    │   │   └── response/       TokenResponse, UserResponse
    │   └── usecases/           *Impl classes
    └── common/
        ├── annotations/        @CriteriaField
        ├── events/             DomainEventPublisher
        ├── mappers/            BaseMapper, BaseEntityResponse
        ├── pagination/         Page<T>, Pageable
        ├── repository/         Repository<D, ID, C>, Criteria
        └── schemas/            UUIDResponse
```

---

## Ports

Ports are interfaces that declare what the application needs from the outside. They live in `application` and are implemented in `infrastructure`.

### `AuthenticationPort`

```java
User authenticate(String email, String rawPassword);
```

Verifies credentials and returns the domain `User`. The implementation delegates to Spring Security's `AuthenticationManager`.

### `PasswordEncoderPort`

```java
String encode(String rawPassword);
boolean matches(String rawPassword, String hashedPassword);
```

Abstracts the hashing algorithm. The implementation uses BCrypt. Swapping to Argon2 or SCrypt requires only changing the `infrastructure` implementation — nothing in `application` or `domain` changes.

### `TokenProviderPort`

```java
TokenResponse generateToken(User user);
TokenResponse refreshToken(String refreshToken);
String validateToken(String token);
```

Abstracts JWT generation and validation. The implementation uses `auth0/java-jwt`.

---

## Repository Contract

`Repository<D, ID, C>` is the generic contract for all repositories in the system:

```java
public interface Repository<D extends DomainEntity<ID>, ID, C extends Criteria> {
    D save(D entity);
    List<D> saveBatch(Iterable<D> entities);
    void delete(D entity);
    void deleteAll(Iterable<D> entities);
    void deleteAll();
    Optional<D> findById(ID id);
    boolean existsById(ID id);
    long count();
    long count(C criteria);
    Page<D> findAll(Pageable pageable);
    List<D> findAll();
    Page<D> findAll(C criteria, Pageable pageable);
}
```

Specific repositories extend this and add domain-specific queries:

```java
public interface UserRepository extends Repository<User, UUID, UserCriteria> {
    Optional<User> findByEmail(String email);
    boolean existsByRole(UserRole role);
}
```

---

## The Criteria System

This is one of the most powerful patterns in this boilerplate.

`Criteria` is a marker interface. Any record that implements it and annotates its fields with `@CriteriaField` becomes a dynamic query filter — automatically, with no extra code.

```java
public record UserCriteria(
    @CriteriaField(value = "name", operator = Operator.LIKE) String name,
    @CriteriaField("role") String role,
    @CriteriaField("active") Boolean active
) implements Criteria {}
```

The `CriteriaUtils` in `infrastructure` reads the record components via reflection at runtime, maps each non-null field to a JPA `Predicate`, and builds the query. Null fields are ignored — making all filters optional by default.

Supported operators: `EQUAL`, `LIKE`, `IN`, `GREATER_THAN`, `LESS_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN_OR_EQUAL`.

Dot notation is supported for nested fields: `@CriteriaField("address.city")`.

To add a new filterable field, add a record component with `@CriteriaField`. Nothing else changes.

---

## Pagination

`Pageable` is a simple record carrying `page`, `size`, `sortBy`, and `sortDirection`. It is framework-agnostic — `PageableAdapter` in `infrastructure` converts it to Spring's `Pageable` when needed.

`Page<T>` is also framework-agnostic and supports `map(Function)` for transforming content without re-querying.

---

## Request Validation

Request records use two complementary validation mechanisms:

- `@NotBlank` / `@NotNull` annotations — enforced by Spring's `@Valid` at the controller layer
- Compact constructors — enforce domain rules immediately at construction time

```java
public record RegisterRequest(...) {
    public RegisterRequest {
        FullName.of(name);
        Email.of(email);
        Password.fromRaw(password);
    }
}
```

This means an invalid request cannot even be constructed — whether it comes from an HTTP call or from a test.

---

## Mappers

`UserMapper` uses MapStruct and extends `BaseMapper`, which provides helpers for unwrapping value objects:

```java
default String fromStringVO(ValueObject<String> vo) {
    return vo == null ? null : vo.getValue();
}
```

MapStruct generates the implementation at compile time. The mapper bean is registered manually in `boot`.

---

## What to keep

- The `Repository` generic contract and `Criteria` system — they are the backbone of data access.
- The port pattern — always declare what you need as an interface here, implement in `infrastructure`.
- The dual validation approach on request records.

## What to extend

- Add new use cases following the same contract / impl pattern.
- Add new criteria records for each new domain — one record per bounded context.
- Add new ports for any external dependency the application needs (email sender, file storage, etc.).

## What you can change

- Response classes — extend `BaseEntityResponse` or use records freely.
- Mapper strategies — MapStruct is the default but `BaseMapper` works with any mapping approach.
- Pagination defaults — change default page size in the controller layer without touching this module.
