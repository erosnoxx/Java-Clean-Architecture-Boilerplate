package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.ports.AuthenticationPort;
import com.boilerplate.application.auth.contracts.ports.TokenProviderPort;
import com.boilerplate.application.auth.contracts.usecases.auth.LoginUseCase;
import com.boilerplate.application.auth.schemas.request.LoginRequest;
import com.boilerplate.application.auth.schemas.response.TokenResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {
    private final AuthenticationPort authenticationPort;
    private final TokenProviderPort tokenProviderPort;

    @Override
    public TokenResponse execute(LoginRequest request) {
        var user = authenticationPort.authenticate(request.email(), request.password());
        return tokenProviderPort.generateToken(user);
    }
}
