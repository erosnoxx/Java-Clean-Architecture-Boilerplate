package com.boilerplate.infrastructure.auth.adapters;

import com.boilerplate.application.auth.contracts.ports.TokenProviderPort;
import com.boilerplate.application.auth.schemas.response.TokenResponse;
import com.boilerplate.domain.auth.entities.User;
import com.boilerplate.infrastructure.auth.data.jpa.UserJpaRepository;
import com.boilerplate.infrastructure.auth.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class TokenProviderAdapter implements TokenProviderPort {
    private final TokenService tokenService;
    private final UserJpaRepository userJpaRepository;


    @Override
    public TokenResponse generateToken(User user) {
        var entity = userJpaRepository.findByEmail(user.getEmail().getValue())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        return tokenService.generateToken(entity);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        return tokenService.refreshToken(refreshToken);
    }

    @Override
    public String validateToken(String token) {
        return tokenService.validateToken(token);
    }
}
