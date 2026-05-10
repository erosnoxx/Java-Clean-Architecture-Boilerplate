package com.boilerplate.application.auth.contracts.repositories;

import com.boilerplate.application.auth.criteria.UserCriteria;
import com.boilerplate.application.common.repository.Repository;
import com.boilerplate.domain.auth.entities.User;
import com.boilerplate.domain.auth.enums.UserRole;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends Repository<User, UUID, UserCriteria> {
    Optional<User> findByEmail(String email);
    boolean existsByRole(UserRole role);
}
