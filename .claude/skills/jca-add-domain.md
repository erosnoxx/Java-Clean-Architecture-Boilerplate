---
name: jca-add-domain
description: Use when creating a new bounded context (domain module) from scratch — covers all 5 Maven modules with a full step-by-step checklist. Always invoke before starting any new domain implementation.
---

# Creating a New Bounded Context

Load `jca-overview` first if you haven't already.

For sub-tasks, invoke the relevant skills:
- `jca-add-value-object` — for each VO
- `jca-add-entity` — for the main aggregate/entity
- `jca-add-use-case` — for each use case
- `jca-add-state-machine` — if the entity has state transitions
- `jca-add-domain-event` — for each domain event

---

## Step-by-Step Checklist

### 1. Domain Module (`domain/src/main/java/com/<project>/<bc>/`)

- [ ] Create package structure: `entities/`, `vos/`, `enums/`, `events/`
- [ ] Create the main entity — invoke `jca-add-entity`
- [ ] Create each Value Object — invoke `jca-add-value-object`
- [ ] Create status enum if entity has states — invoke `jca-add-state-machine`
- [ ] Create domain events as records — invoke `jca-add-domain-event`

### 2. Application Module (`application/src/main/java/com/<project>/<bc>/`)

- [ ] `contracts/repositories/<Entity>Repository.java`

```java
package com.<project>.<bc>.contracts.repositories;

import com.<project>.application.common.repository.Repository;
import com.<project>.<bc>.criteria.<Entity>Criteria;
import com.<project>.<bc>.entities.<Entity>;
import java.util.UUID;

public interface <Entity>Repository extends Repository<<Entity>, UUID, <Entity>Criteria> {}
```

- [ ] `criteria/<Entity>Criteria.java`

```java
package com.<project>.<bc>.criteria;

import com.<project>.application.common.annotations.CriteriaField;
import com.<project>.application.common.annotations.CriteriaField.Operator;
import com.<project>.application.common.repository.Criteria;

public record <Entity>Criteria(
    @CriteriaField(value = "name", operator = Operator.LIKE) String name
    // add more fields as needed
) implements Criteria {}
```

- [ ] `schemas/response/<Entity>Response.java` — record with all public-facing fields
- [ ] `schemas/request/` — one request record per write use case
- [ ] `mappers/<Entity>Mapper.java` — MapStruct interface

```java
@Mapper
public interface <Entity>Mapper extends BaseMapper {
    @Mapping(target = "name", expression = "java(fromStringVO(entity.getName()))")
    <Entity>Response toResponse(<Entity> entity);
}
```

- [ ] `contracts/usecases/` — one interface per use case
- [ ] `usecases/` — one impl per use case — invoke `jca-add-use-case`

### 3. Infrastructure Module (`infrastructure/src/main/java/com/<project>/<bc>/`)

- [ ] `data/entities/<Entity>Entity.java`

```java
@Entity @Table(name = "<entities>")
@Getter @Setter @NoArgsConstructor
public class <Entity>Entity extends BaseJpaEntity<UUID> {
    // fields matching the domain entity, using primitive types or enums
}
```

- [ ] `data/jpa/<Entity>JpaRepository.java`

```java
public interface <Entity>JpaRepository extends JpaRepository<<Entity>Entity, UUID> {
    Optional<<Entity>Entity> findByEmail(String email); // example
}
```

- [ ] `mappers/<Entity>EntityMapper.java`

```java
public class <Entity>EntityMapper implements EntityMapper<<Entity>, <Entity>Entity> {
    @Override public <Entity>Entity toPersistence(<Entity> domain) { ... }
    @Override public <Entity> toDomain(<Entity>Entity persistence) {
        return <Entity>.reconstitute(
            persistence.getId(),
            // ... all fields ...
            persistence.getCreatedAt(),
            persistence.getUpdatedAt()
        );
    }
}
```

- [ ] `repositories/<Entity>RepositoryImpl.java`

```java
public class <Entity>RepositoryImpl
    extends RepositoryImpl<<Entity>, UUID, <Entity>Criteria, <Entity>Entity, <Entity>JpaRepository>
    implements <Entity>Repository {

    public <Entity>RepositoryImpl(<Entity>JpaRepository jpa, EntityManager em, <Entity>EntityMapper mapper) {
        super(jpa, em, mapper, <Entity>Entity.class);
    }
}
```

- [ ] `listeners/<Bc>EventListener.java` — invoke `jca-add-domain-event`

### 4. Web Module (`web/src/main/java/com/<project>/web/controllers/`)

- [ ] `<Bc>Controller.java`

```java
@RestController
@RequestMapping("/api/<bc>")
@RequiredArgsConstructor
public class <Bc>Controller {
    private final <Entity>Mapper mapper;
    private final List<Entity>UseCase list<Entity>UseCase;
    // inject use case interfaces, never impls
}
```

### 5. Boot Module — Composition Root

- [ ] `boot/config/beans/application/<Bc>Beans.java`

```java
@Configuration @RequiredArgsConstructor
public class <Bc>Beans {
    private final <Entity>Repository repository;
    private final DomainEventPublisher publisher;
    private final <Entity>Mapper mapper;

    @Bean public Create<Entity>UseCase create<Entity>UseCase() {
        return new Create<Entity>UseCaseImpl(repository, publisher);
    }
    // one @Bean per use case
}
```

- [ ] Add to `boot/config/beans/infra/InfraRepositoryBeans.java`:

```java
@Bean public <Entity>Repository <entity>Repository(<Entity>JpaRepository jpa, EntityManager em, <Entity>EntityMapper mapper) {
    return new <Entity>RepositoryImpl(jpa, em, mapper);
}
```

- [ ] Add to `boot/config/beans/infra/InfraMapperBeans.java`:

```java
@Bean public <Entity>EntityMapper <entity>EntityMapper() { return new <Entity>EntityMapper(); }
```

- [ ] Add mapper bean to `boot/config/beans/application/MapperBeans.java`:

```java
@Bean public <Entity>Mapper <entity>Mapper() { return Mappers.getMapper(<Entity>Mapper.class); }
```

- [ ] If listeners exist: add to `boot/config/beans/infra/InfraListenerBeans.java`

---

## Common Pitfalls

- **Forgetting boot wiring** — the most common error. Every class needs a `@Bean`
- **Cross-domain import in domain layer** — if you need data from another BC, use `jca-add-query-adapter`
- **`@Component` on application/infra classes** — never. Manual wiring only
- **Business logic in use cases** — use cases orchestrate; business rules live in the domain entity
- **Using `findById(...).orElseThrow(...)` inline** — use `QueryUtils.findOrThrow(repo, id, msg)` instead
