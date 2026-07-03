package com.boilerplate.domain.auth.entities;

import com.boilerplate.domain.auth.enums.UserRole;
import com.boilerplate.domain.auth.enums.UserStatus;
import com.boilerplate.domain.auth.events.UserCreatedEvent;
import com.boilerplate.domain.auth.statemachine.UserStateMachine;
import com.boilerplate.domain.shared.vos.Email;
import com.boilerplate.domain.auth.vos.FullName;
import com.boilerplate.domain.auth.vos.Password;
import com.boilerplate.domain.common.entities.DomainEntity;
import lombok.Getter;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public class User extends DomainEntity<UUID> {

    private FullName name;
    private Email email;
    private Password password;
    private UserRole role;
    private UserStatus status;

    private User() {}

    public static User create(
            FullName name,
            Email email,
            Password hashedPassword,
            UserRole role
    ) {
        checkIfPasswordIsHashed(hashedPassword);
        var user = new User();
        user.setId(UUID.randomUUID());
        user.name = name;
        user.email = email;
        user.password = hashedPassword;
        user.role = role;
        user.status = UserStatus.ACTIVE;

        user.registerEvent(new UserCreatedEvent(
                user.getId(),
                email.getValue()
        ));

        return user;
    }

    public static User reconstitute(
            UUID id,
            FullName name,
            Email email,
            Password hashedPassword,
            UserRole role,
            UserStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        checkIfPasswordIsHashed(hashedPassword);
        var user = new User();
        user.setId(id);
        user.name = name;
        user.email = email;
        user.password = hashedPassword;
        user.role = role;
        user.status = status;
        user.setTimestamps(createdAt, updatedAt);
        return user;
    }

    public void activate() { transition(UserStateMachine::activate); }
    public void deactivate() { transition(UserStateMachine::deactivate); }
    public void suspend() { transition(UserStateMachine::suspend); }

    public boolean isActive() { return status == UserStatus.ACTIVE; }

    private void transition(Consumer<UserStateMachine> action) {
        var sm = UserStateMachine.of(this);
        action.accept(sm);
        this.status = sm.getValue();
        var event = sm.getEvent();
        if (event != null) registerEvent(event);
    }

    public void promote() {
        if (role == UserRole.ADMIN)
            return;
        role = UserRole.ADMIN;
    }

    public void demote() {
        if (role == UserRole.USER)
            return;
        role = UserRole.USER;
    }

    public void changePassword(Password hashedPassword) {
        checkIfPasswordIsHashed(hashedPassword);
        this.password = hashedPassword;
    }

    public void changeEmail(Email email) {
        this.email = email;
    }

    public void rename(FullName name) {
        this.name = name;
    }

    private static void checkIfPasswordIsHashed(Password password) {
        if (!password.isHashed())
            throw new IllegalArgumentException("password must be hashed");
    }
}
