package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.users.GetUserByEmailUseCase;
import com.boilerplate.application.auth.mappers.UserMapper;
import com.boilerplate.application.auth.schemas.response.UserResponse;
import com.boilerplate.domain.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetUserByEmailUseCaseImpl implements GetUserByEmailUseCase {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public UserResponse execute(String email) {
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return mapper.toResponse(user);
    }
}
