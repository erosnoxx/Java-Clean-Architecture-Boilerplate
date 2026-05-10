package com.boilerplate.application.auth.contracts.usecases.users;

import com.boilerplate.application.auth.schemas.response.UserResponse;

import java.util.UUID;

public interface GetUserByIdUseCase {
    UserResponse execute(UUID id);
}
