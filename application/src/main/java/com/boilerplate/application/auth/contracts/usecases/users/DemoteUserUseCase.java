package com.boilerplate.application.auth.contracts.usecases.users;

import java.util.UUID;

public interface DemoteUserUseCase {
    void execute(UUID id);
}
