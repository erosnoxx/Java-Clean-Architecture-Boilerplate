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
