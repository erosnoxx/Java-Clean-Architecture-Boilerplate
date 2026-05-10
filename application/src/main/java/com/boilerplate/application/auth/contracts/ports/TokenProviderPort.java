package com.boilerplate.application.auth.contracts.ports;

import com.boilerplate.application.auth.schemas.response.TokenResponse;
import com.boilerplate.domain.auth.entities.User;

public interface TokenProviderPort {
    TokenResponse generateToken(User user);
    TokenResponse refreshToken(String refreshToken);
    String validateToken(String token);
}
