package com.boilerplate.infrastructure.auth.repositories;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.criteria.UserCriteria;
import com.boilerplate.domain.auth.entities.User;
import com.boilerplate.domain.auth.enums.UserRole;
import com.boilerplate.infrastructure.auth.data.entities.UserEntity;
import com.boilerplate.infrastructure.auth.data.jpa.UserJpaRepository;
import com.boilerplate.infrastructure.auth.mappers.UserEntityMapper;
import com.boilerplate.infrastructure.common.persistence.repositories.RepositoryImpl;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public class UserRepositoryImpl
        extends RepositoryImpl<User, UUID, UserCriteria, UserEntity, UserJpaRepository>
        implements UserRepository {

    public UserRepositoryImpl(
            UserJpaRepository jpaRepository,
            EntityManager em,
            UserEntityMapper mapper) {
        super(jpaRepository, em, mapper, UserEntity.class);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByRole(UserRole role) {
        return jpaRepository.existsByRole(role);
    }
}
