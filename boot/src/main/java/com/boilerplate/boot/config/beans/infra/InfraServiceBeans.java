package com.boilerplate.boot.config.beans.infra;

import com.boilerplate.application.auth.contracts.ports.PasswordEncoderPort;
import com.boilerplate.infrastructure.auth.adapters.PasswordEncoderAdapter;
import com.boilerplate.infrastructure.auth.data.jpa.UserJpaRepository;
import com.boilerplate.infrastructure.auth.security.AuthorizationService;
import com.boilerplate.infrastructure.auth.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration @RequiredArgsConstructor
public class InfraServiceBeans {
    @Value("${api.security.token.secret}")
    private String tokenSecret;
    @Value("${api.security.token.expirationTimeInHours}")
    private Long tokenExpirationTimeInHours;
    @Value("${api.security.token.refreshExpirationTimeInHours}")
    private Long refreshTokenExpirationTimeInHours;

    private final PasswordEncoder passwordEncoder;
    private final UserJpaRepository userJpaRepository;

    @Bean
    public TokenService tokenService() {
        return new TokenService(
            tokenSecret, tokenExpirationTimeInHours, refreshTokenExpirationTimeInHours
        );
    }

    @Bean
    public AuthorizationService authorizationService() {
        return new AuthorizationService(userJpaRepository);
    }

    @Bean
    public PasswordEncoderPort passwordEncoderPort() {
        return new PasswordEncoderAdapter(
                passwordEncoder
        );
    }
}
