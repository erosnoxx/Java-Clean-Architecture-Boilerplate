package com.boilerplate.application.auth.contracts.usecases.users;

import com.boilerplate.application.auth.schemas.response.UserResponse;

public interface GetUserByEmailUseCase {
    UserResponse execute(String email);
}
