package com.boilerplate.application.auth.contracts.usecases.auth;

import com.boilerplate.application.auth.schemas.request.RefreshTokenRequest;
import com.boilerplate.application.auth.schemas.response.TokenResponse;

public interface RefreshTokenUseCase {
    TokenResponse execute(RefreshTokenRequest request);
}
