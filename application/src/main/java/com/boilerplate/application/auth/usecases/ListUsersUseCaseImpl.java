package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.users.ListUsersUseCase;
import com.boilerplate.application.auth.criteria.UserCriteria;
import com.boilerplate.application.auth.mappers.UserMapper;
import com.boilerplate.application.auth.schemas.response.UserResponse;
import com.boilerplate.application.common.pagination.Page;
import com.boilerplate.application.common.pagination.Pageable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ListUsersUseCaseImpl implements ListUsersUseCase {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public Page<UserResponse> execute(UserCriteria criteria, Pageable pageable) {
        return repository.findAll(criteria, pageable)
                .map(mapper::toResponse);
    }
}
