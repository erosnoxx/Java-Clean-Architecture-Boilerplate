package com.boilerplate.application.auth.contracts.usecases.users;

import com.boilerplate.application.auth.schemas.request.UpdateUserRequest;
import com.boilerplate.application.common.schemas.UUIDResponse;

import java.util.UUID;

public interface UpdateUserUseCase {
    UUIDResponse execute(UUID id, UpdateUserRequest request);
}
