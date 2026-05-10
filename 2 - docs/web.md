# Web Module

The web module is the entry point for HTTP traffic. It receives requests, delegates to use cases, and returns responses. It knows about HTTP — and nothing else.

Controllers in this module depend only on `application` interfaces. They never call repositories, services, or any infrastructure class directly.

---

## Structure

```
web/
└── com.boilerplate.web/
    ├── controllers/    AuthController, UserController
    ├── middlewares/    RestExceptionHandler
    └── security/       @AdminOnly, @AuthenticatedOnly
```

---

## Controllers

### `AuthController` — `/auth`

| Method | Route | Access | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Authenticates and returns token pair |
| POST | `/auth/refresh` | Public | Refreshes token pair |
| POST | `/auth/register` | `@AdminOnly` | Creates a new user |

### `UserController` — `/users`

| Method | Route | Access | Description |
|---|---|---|---|
| GET | `/users/{id}` | Authenticated | Get user by ID |
| GET | `/users/email/{email}` | Authenticated | Get user by email |
| GET | `/users` | `@AdminOnly` | List users with filters and pagination |
| PUT | `/users/{id}` | Authenticated | Update name and/or email |
| PATCH | `/users/{id}/password` | Authenticated | Change password |
| PATCH | `/users/{id}/toggle` | `@AdminOnly` | Toggle active status |
| PATCH | `/users/{id}/promote` | `@AdminOnly` | Promote to admin |
| PATCH | `/users/{id}/demote` | `@AdminOnly` | Demote to user |

---

## Security Annotations

Authorization is handled at the method level via custom annotations backed by `@PreAuthorize`. Enabled by `@EnableMethodSecurity` in `SecurityConfiguration`.

```java
@AdminOnly          // @PreAuthorize("hasRole('ADMIN')")
@AuthenticatedOnly  // @PreAuthorize("isAuthenticated()")
```

Usage:

```java
@AdminOnly
@GetMapping
public ResponseEntity<Page<UserResponse>> list(...) { ... }
```

To add a new role or permission:

1. Add the role to `UserRole` in `domain`
2. Update `getAuthorities()` in `UserEntity` in `infrastructure`
3. Create a new annotation in `web/security/` backed by the appropriate `@PreAuthorize` expression

The rest of the system does not need to change.

---

## Exception Handling

`RestExceptionHandler` is a `@RestControllerAdvice` that maps exceptions to RFC 7807 `ProblemDetail` responses.

| Exception | Status | Trigger |
|---|---|---|
| `NotFoundException` | 404 | Entity not found |
| `ConflictException` | 409 | Duplicate data |
| `DomainException` | 422 | Business rule violation |
| `MethodArgumentNotValidException` | 400 | `@Valid` failure — returns list of `InvalidParam` |
| `HttpMessageNotReadableException` | 400 | Malformed or missing request body |
| `MissingServletRequestParameterException` | 400 | Required `@RequestParam` absent |
| `MethodArgumentTypeMismatchException` | 400 | Wrong type in path or param (e.g. text where UUID expected) |
| `HttpRequestMethodNotSupportedException` | 405 | Wrong HTTP method for the route |
| `NoResourceFoundException` | 404 | Route does not exist |
| `AccessDeniedException` | 403 | Authenticated but not authorized |
| `AuthenticationException` | 401 | Invalid or missing credentials |
| `Exception` | 500 | Unexpected error — logged with full stack trace |

Validation errors return a structured body:

```json
{
  "status": 400,
  "title": "validation failed",
  "errors": [
    { "name": "email", "reason": "must not be blank" },
    { "name": "password", "reason": "must not be blank" }
  ]
}
```

---

## What to keep

- `RestExceptionHandler` — it covers the full exception surface. Extend it, never replace it.
- The annotation-based authorization pattern — it is declarative, readable, and easy to audit.

## What to extend

- Add new controllers following the same pattern: inject use case interfaces, never concrete implementations.
- Add new security annotations for new roles or permission combinations.
- Add new exception handlers for domain-specific exceptions as the system grows.

## What you can change

- Response formats — `ProblemDetail` is the default but you can wrap it in a custom envelope if needed.
- Route prefixes — change `@RequestMapping` values freely without affecting any other layer.
- Validation approach — `@Valid` with Bean Validation is the default, but custom validators can be added at the record level.
