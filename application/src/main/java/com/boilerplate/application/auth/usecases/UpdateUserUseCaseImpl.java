package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.users.UpdateUserUseCase;
import com.boilerplate.application.auth.schemas.request.UpdateUserRequest;
import com.boilerplate.application.common.schemas.UUIDResponse;
import com.boilerplate.domain.auth.vos.FullName;
import com.boilerplate.domain.common.exceptions.NotFoundException;
import com.boilerplate.domain.shared.vos.Email;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {
    private final UserRepository repository;

    @Override
    public UUIDResponse execute(UUID id, UpdateUserRequest request) {
        var user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        request.name().map(FullName::of).ifPresent(user::rename);
        request.email().map(Email::of).ifPresent(user::changeEmail);

        repository.save(user);

        return UUIDResponse.of(user.getId());
    }
}
