package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.users.GetUserByIdUseCase;
import com.boilerplate.application.auth.mappers.UserMapper;
import com.boilerplate.application.auth.schemas.response.UserResponse;
import com.boilerplate.domain.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetUserByIdUseCaseImpl implements GetUserByIdUseCase {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public UserResponse execute(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return mapper.toResponse(user);
    }
}
