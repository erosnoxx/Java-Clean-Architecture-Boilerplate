package com.boilerplate.infrastructure.common.persistence.entities;

import com.boilerplate.infrastructure.common.utils.TimeConfig;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@MappedSuperclass
@Getter @Setter
public abstract class BaseJpaEntity<ID> implements PersistenceEntity<ID> {
    @Id
    private ID id;
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(TimeConfig.DEFAULT_OFFSET);
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = OffsetDateTime.now(TimeConfig.DEFAULT_OFFSET);
    }
}
