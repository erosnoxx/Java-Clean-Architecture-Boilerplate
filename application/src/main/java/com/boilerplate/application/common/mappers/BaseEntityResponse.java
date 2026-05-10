package com.boilerplate.application.common.mappers;

import java.time.OffsetDateTime;
import java.util.UUID;

public abstract class BaseEntityResponse {
    public final UUID id;
    public final OffsetDateTime createdAt;
    public final OffsetDateTime updatedAt;

    public BaseEntityResponse(UUID id, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
