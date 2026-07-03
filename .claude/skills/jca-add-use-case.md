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
