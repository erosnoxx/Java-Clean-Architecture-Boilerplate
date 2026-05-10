package com.boilerplate.infrastructure.auth.data.jpa;

import com.boilerplate.domain.auth.enums.UserRole;
import com.boilerplate.infrastructure.auth.data.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByRole(UserRole role);
}
