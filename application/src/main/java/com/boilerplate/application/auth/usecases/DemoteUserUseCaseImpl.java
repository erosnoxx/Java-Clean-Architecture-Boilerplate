package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.users.DemoteUserUseCase;
import com.boilerplate.domain.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DemoteUserUseCaseImpl implements DemoteUserUseCase {
    private final UserRepository repository;

    @Override
    public void execute(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.demote();
        repository.save(user);
    }
}
