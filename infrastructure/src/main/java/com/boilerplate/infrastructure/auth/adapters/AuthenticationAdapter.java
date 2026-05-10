package com.boilerplate.infrastructure.auth.adapters;

import com.boilerplate.application.auth.contracts.ports.AuthenticationPort;
import com.boilerplate.domain.auth.entities.User;
import com.boilerplate.infrastructure.auth.data.entities.UserEntity;
import com.boilerplate.infrastructure.auth.mappers.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

@RequiredArgsConstructor
public class AuthenticationAdapter implements AuthenticationPort {
    private final AuthenticationManager authenticationManager;
    private final UserEntityMapper mapper;

    @Override
    public User authenticate(String email, String rawPassword) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(email, rawPassword);
            var authentication = authenticationManager.authenticate(usernamePassword);

            var entity = (UserEntity) authentication.getPrincipal();

            return mapper.toDomain(entity);
        } catch (AuthenticationException e) {
            throw new RuntimeException("invalid credentials", e);
        }
    }
}
