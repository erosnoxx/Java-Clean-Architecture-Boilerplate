package com.boilerplate.application.auth.schemas.response;

import com.boilerplate.application.common.mappers.BaseEntityResponse;
import com.boilerplate.domain.auth.enums.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserResponse extends BaseEntityResponse {
    public final String name;
    public final String email;
    public final UserRole role;
    public final boolean active;

    public UserResponse(
            UUID id,
            OffsetDateTime createdAt, OffsetDateTime updatedAt,
            String name, String email,
            UserRole role, boolean active) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
    }
}
