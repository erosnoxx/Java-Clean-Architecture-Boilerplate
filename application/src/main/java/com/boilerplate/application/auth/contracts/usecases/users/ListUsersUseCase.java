package com.boilerplate.application.auth.contracts.usecases.users;

import com.boilerplate.application.auth.criteria.UserCriteria;
import com.boilerplate.application.auth.schemas.response.UserResponse;
import com.boilerplate.application.common.pagination.Page;
import com.boilerplate.application.common.pagination.Pageable;

public interface ListUsersUseCase {
    Page<UserResponse> execute(UserCriteria criteria, Pageable pageable);
}
