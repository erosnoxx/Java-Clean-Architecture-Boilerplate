package com.boilerplate.boot.config.beans.infra;

import com.boilerplate.application.auth.contracts.ports.AuthenticationPort;
import com.boilerplate.application.auth.contracts.ports.TokenProviderPort;
import com.boilerplate.infrastructure.auth.adapters.AuthenticationAdapter;
import com.boilerplate.infrastructure.auth.adapters.TokenProviderAdapter;
import com.boilerplate.infrastructure.auth.data.jpa.UserJpaRepository;
import com.boilerplate.infrastructure.auth.mappers.UserEntityMapper;
import com.boilerplate.infrastructure.auth.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
@RequiredArgsConstructor
public class InfraAuthBeans {
    private final AuthenticationManager authenticationManager;
    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;
    private final TokenService tokenService;

    @Bean
    public AuthenticationPort authenticationPort() {
        return new AuthenticationAdapter(
                authenticationManager, userEntityMapper
        );
    }

    @Bean
    public TokenProviderPort tokenProviderPort() {
        return new TokenProviderAdapter(
                tokenService, userJpaRepository
        );
    }
}
