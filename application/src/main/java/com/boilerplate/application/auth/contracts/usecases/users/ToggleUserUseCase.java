package com.boilerplate.application.auth.contracts.usecases.users;

import java.util.UUID;

public interface ToggleUserUseCase {
    void execute(UUID id);
}
