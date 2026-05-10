package com.boilerplate.application.auth.contracts.usecases.users;

import java.util.UUID;

public interface PromoteUserUseCase {
    void execute(UUID id);
}
