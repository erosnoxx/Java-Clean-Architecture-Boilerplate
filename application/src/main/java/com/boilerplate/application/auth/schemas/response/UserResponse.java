package com.boilerplate.application.auth.schemas.response;

import com.boilerplate.application.common.mappers.BaseEntityResponse;
import com.boilerplate.domain.auth.enums.UserRole;
import com.boilerplate.domain.auth.enums.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserResponse extends BaseEntityResponse {
    public final String name;
    public final String email;
    public final UserRole role;
    public final UserStatus status;

    public UserResponse(
            UUID id,
            OffsetDateTime createdAt, OffsetDateTime updatedAt,
            String name, String email,
            UserRole role, UserStatus status) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }
}
