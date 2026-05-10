package com.boilerplate.application.auth.contracts.usecases.users;

import com.boilerplate.application.auth.schemas.request.ChangePasswordRequest;

import java.util.UUID;

public interface ChangePasswordUseCase {
    void execute(UUID id, ChangePasswordRequest request);
}
