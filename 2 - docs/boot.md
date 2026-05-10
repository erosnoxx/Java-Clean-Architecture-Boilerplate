# Boot Module

The boot module starts the application. It is the only module that sees everything — and that is by design. Its job is to wire all the pieces together, configure the Spring context, and get out of the way.

No business logic lives here. No domain rules. No use case implementations. Only configuration and assembly.

---

## Structure

```
boot/
└── com.boilerplate.boot/
    ├── common/
    │   └── seeder/         Seeder<T>, DatabaseSeeder
    ├── config/
    │   ├── beans/
    │   │   ├── application/ ApplicationAuthBeans, MapperBeans
    │   │   └── infra/       InfraAuthBeans, InfraCryptoBeans, InfraMapperBeans,
    │   │                    InfraPublisherBeans, InfraRepositoryBeans, InfraServiceBeans
    │   ├── cors/            CorsConfig
    │   ├── security/        SecurityConfiguration, SecurityFilter, SecurityMatchers
    │   └── seeder/          AdminSeeder
    └── BootApplication.java
```

---

## Manual Bean Wiring

Spring's component scan is intentionally limited. Beans in `infrastructure` and `application` carry no `@Service` or `@Component` annotations — they are instantiated explicitly in `@Configuration` classes inside `boot`.

This is a deliberate architectural choice. See [`docs/decisions.md`](decisions.md) for the full reasoning.

The bean configuration is organized by layer and concern:

| Class | Responsibility |
|---|---|
| `InfraMapperBeans` | JPA entity mappers |
| `InfraRepositoryBeans` | Repository implementations |
| `InfraServiceBeans` | TokenService, AuthorizationService, PasswordEncoderPort |
| `InfraAuthBeans` | AuthenticationPort, TokenProviderPort |
| `InfraCryptoBeans` | PasswordEncoder (BCrypt) |
| `InfraPublisherBeans` | DomainEventPublisher |
| `ApplicationAuthBeans` | All use case implementations |
| `MapperBeans` | MapStruct mappers |

---

## Security Configuration

`SecurityConfiguration` defines the `SecurityFilterChain`:

- CSRF disabled (stateless JWT API)
- Session management set to `STATELESS`
- Public endpoints declared in `SecurityMatchers`
- JWT filter added before `UsernamePasswordAuthenticationFilter`
- Method-level security enabled via `@EnableMethodSecurity`
- CORS configured from `application.properties`

### Public Endpoints

```java
new AntPathRequestMatcher("/auth/login"),
new AntPathRequestMatcher("/auth/refresh")
```

All other routes require authentication by default. Authorization is further controlled at the method level via `@AdminOnly` and `@AuthenticatedOnly`.

### JWT Filter

`SecurityFilter` intercepts every request, extracts the `Bearer` token, validates it via `TokenService`, loads the user via `AuthorizationService`, and sets the authentication in the `SecurityContextHolder`.

If the token is absent or invalid, the request continues unauthenticated — Spring Security handles the 401 response downstream.

---

## CORS

`CorsConfig` reads allowed origins from:

```properties
cors.allowed-origins=${CORS_ORIGINS}
```

Allowed methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
Allowed headers: `Authorization`, `Content-Type`.
Exposed headers: `Authorization`.

To add a new frontend origin, update the environment variable — no code change required.

---

## Database Seeder

The seeder system is built around two abstractions:

### `Seeder<T>`

```java
public interface Seeder<T> {
    void seed(List<T> data);
    String resourcePath();
    Class<T> recordType();
}
```

Each seeder declares where its data lives and what type to deserialize it into.

### `DatabaseSeeder`

Implements `ApplicationRunner` — runs after the Spring context is fully started. Iterates all `Seeder` beans, reads the JSON file, deserializes the records, and calls `seed()`.

```
boot/src/main/resources/seeders/
└── users.json
```

Seeders are idempotent — they check for existing records before inserting.

To add a new seeder:

1. Create a JSON file in `resources/seeders/`
2. Create a class implementing `Seeder<YourRecord>`
3. Register it as a `@Component` — `DatabaseSeeder` picks it up automatically via `List<Seeder>` injection

---

## Profiles

| Profile | Database | Flyway | Use case |
|---|---|---|---|
| `dev` | PostgreSQL (local) | Enabled | Local development |
| `prod` | PostgreSQL | Enabled | Production |

Profile is set via `ACTIVE_PROFILE` environment variable. Defaults to `dev`.

---

## Environment Variables

| Variable | Description | Required |
|---|---|---|
| `ACTIVE_PROFILE` | Spring profile (`dev` / `prod`) | Yes |
| `SERVER_PORT` | HTTP port | Yes |
| `DB_HOST` | Database host | Yes |
| `DB_PORT` | Database port | Yes |
| `DB_NAME` | Database name | Yes |
| `DB_SCHEMA` | Database schema | Yes |
| `DB_USER` | Database user | Yes |
| `DB_PWD` | Database password | Yes |
| `SEC_KEY` | JWT signing secret | Yes |
| `CORS_ORIGINS` | Comma-separated allowed origins | Yes |

---

## What to keep

- The manual bean wiring pattern — it is explicit, auditable, and keeps the architecture honest.
- The seeder abstraction — it scales to any number of seeders without changing `DatabaseSeeder`.
- The security filter chain structure.

## What to extend

- Add new `@Configuration` bean classes as new domain modules are added.
- Add new seeders for new domain data.
- Add new public endpoints to `SecurityMatchers` as needed.

## What you can change

- CORS configuration — origins, methods, and headers are all configurable.
- JWT filter behavior — `SecurityFilter` can be extended to extract claims beyond the subject.
- Profile structure — add new profiles (e.g., `staging`) by adding a new `application-staging.properties`.
