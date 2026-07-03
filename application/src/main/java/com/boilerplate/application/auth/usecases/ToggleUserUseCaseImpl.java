package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.users.ToggleUserUseCase;
import com.boilerplate.domain.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ToggleUserUseCaseImpl implements ToggleUserUseCase {
    private final UserRepository repository;

    @Override
    public void execute(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isActive()) user.deactivate(); else user.activate();
        repository.save(user);
    }
}
