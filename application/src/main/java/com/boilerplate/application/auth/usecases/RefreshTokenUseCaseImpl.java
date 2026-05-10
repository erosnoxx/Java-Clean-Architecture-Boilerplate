package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.ports.TokenProviderPort;
import com.boilerplate.application.auth.contracts.usecases.auth.RefreshTokenUseCase;
import com.boilerplate.application.auth.schemas.request.RefreshTokenRequest;
import com.boilerplate.application.auth.schemas.response.TokenResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {
    private final TokenProviderPort tokenProviderPort;

    @Override
    public TokenResponse execute(RefreshTokenRequest request) {
        return tokenProviderPort.refreshToken(request.refreshToken());
    }
}
