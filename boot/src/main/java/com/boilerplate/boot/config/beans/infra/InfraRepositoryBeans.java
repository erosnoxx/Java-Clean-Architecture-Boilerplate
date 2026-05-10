package com.boilerplate.boot.config.beans.infra;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.infrastructure.auth.data.jpa.UserJpaRepository;
import com.boilerplate.infrastructure.auth.mappers.UserEntityMapper;
import com.boilerplate.infrastructure.auth.repositories.UserRepositoryImpl;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration @RequiredArgsConstructor
public class InfraRepositoryBeans {
    private final EntityManager entityManager;
    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;

    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl(
                userJpaRepository, entityManager, userEntityMapper
        );
    }
}
