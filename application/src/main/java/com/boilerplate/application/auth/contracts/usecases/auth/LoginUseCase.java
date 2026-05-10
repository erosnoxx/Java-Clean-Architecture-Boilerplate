package com.boilerplate.application.auth.contracts.usecases.auth;

import com.boilerplate.application.auth.schemas.request.LoginRequest;
import com.boilerplate.application.auth.schemas.response.TokenResponse;

public interface LoginUseCase {
    TokenResponse execute(LoginRequest request);
}
