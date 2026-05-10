package com.boilerplate.infrastructure.auth.mappers;

import com.boilerplate.domain.auth.entities.User;
import com.boilerplate.domain.shared.vos.Email;
import com.boilerplate.domain.auth.vos.FullName;
import com.boilerplate.domain.auth.vos.Password;
import com.boilerplate.infrastructure.auth.data.entities.UserEntity;
import com.boilerplate.infrastructure.common.mapper.EntityMapper;

public class UserEntityMapper implements EntityMapper<User, UserEntity> {
    @Override
    public UserEntity toPersistence(User domain) {
        var entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName().getValue());
        entity.setEmail(domain.getEmail().getValue());
        entity.setRole(domain.getRole());
        entity.setPasswordHash(domain.getPassword().getValue());
        entity.setActive(domain.isActive());

        return entity;
    }

    @Override
    public User toDomain(UserEntity persistence) {
        var user = User.reconstitute(
                persistence.getId(),
                FullName.of(persistence.getName()),
                Email.of(persistence.getEmail()),
                Password.fromHashed(persistence.getPasswordHash()),
                persistence.getRole(),
                persistence.isActive()
        );

        user.setCreatedAt(persistence.getCreatedAt());
        user.setUpdatedAt(persistence.getUpdatedAt());
        return user;
    }
}
