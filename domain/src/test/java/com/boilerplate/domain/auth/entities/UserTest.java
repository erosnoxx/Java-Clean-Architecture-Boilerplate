package com.boilerplate.domain.auth.entities;

import com.boilerplate.domain.auth.enums.UserRole;
import com.boilerplate.domain.auth.enums.UserStatus;
import com.boilerplate.domain.auth.events.UserActivatedEvent;
import com.boilerplate.domain.auth.events.UserCreatedEvent;
import com.boilerplate.domain.auth.events.UserDeactivatedEvent;
import com.boilerplate.domain.auth.events.UserSuspendedEvent;
import com.boilerplate.domain.auth.vos.FullName;
import com.boilerplate.domain.auth.vos.Password;
import com.boilerplate.domain.common.exceptions.DomainException;
import com.boilerplate.domain.shared.vos.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Should create active user successfully")
    void shouldCreateUserSuccessfully() {
        var name = FullName.of("João Silva");
        var email = Email.of("joao@example.com");
        var password = Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv");
        var role = UserRole.USER;

        var user = User.create(name, email, password, role);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getRole()).isEqualTo(role);

        assertThat(user.isActive()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

        // lifecycle fields should start null until persistence layer fills them
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should generate unique ids for created users")
    void shouldGenerateUniqueIds() {
        var user1 = User.create(
                FullName.of("João Silva"),
                Email.of("joao1@example.com"),
                Password.fromHashed("$2a$10$hash111111111111111111"),
                UserRole.USER
        );

        var user2 = User.create(
                FullName.of("Maria Silva"),
                Email.of("maria@example.com"),
                Password.fromHashed("$2a$10$hash222222222222222222"),
                UserRole.USER
        );

        assertThat(user1.getId()).isNotEqualTo(user2.getId());
    }

    @Test
    @DisplayName("Should register UserCreatedEvent when user is created")
    void shouldRegisterUserCreatedEvent() {
        var email = Email.of("joao@example.com");

        var user = User.create(
                FullName.of("João Silva"),
                email,
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        var events = user.pullEvents();

        assertThat(events).hasSize(1);

        assertThat(events.getFirst())
                .isInstanceOf(UserCreatedEvent.class);

        var event = (UserCreatedEvent) events.getFirst();

        assertThat(event.userId()).isEqualTo(user.getId());
        assertThat(event.email()).isEqualTo(email.getValue());
    }

    @Test
    @DisplayName("Should clear events after pulling them")
    void shouldClearEventsAfterPulling() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        var firstPull = user.pullEvents();
        var secondPull = user.pullEvents();

        assertThat(firstPull).hasSize(1);
        assertThat(secondPull).isEmpty();
    }

    @Test
    @DisplayName("Should return immutable event list")
    void shouldReturnImmutableEventList() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        var events = user.pullEvents();

        assertThatThrownBy(() ->
                events.add(null)
        )
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should reconstitute user without registering events")
    void shouldReconstituteUserWithoutEvents() {
        var id = UUID.randomUUID();
        var createdAt = OffsetDateTime.now().minusDays(5);
        var updatedAt = OffsetDateTime.now();

        var user = User.reconstitute(
                id,
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.ADMIN,
                UserStatus.INACTIVE,
                createdAt,
                updatedAt
        );

        assertThat(user.getId()).isEqualTo(id);

        assertThat(user.getName().getValue())
                .isEqualTo("João Silva");

        assertThat(user.getEmail().getValue())
                .isEqualTo("joao@example.com");

        assertThat(user.getRole())
                .isEqualTo(UserRole.ADMIN);

        assertThat(user.isActive()).isFalse();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);

        assertThat(user.getCreatedAt())
                .isEqualTo(createdAt);

        assertThat(user.getUpdatedAt())
                .isEqualTo(updatedAt);

        assertThat(user.pullEvents()).isEmpty();
    }

    @Test
    @DisplayName("Should reject raw password on user creation")
    void shouldRejectRawPasswordOnCreate() {
        var rawPassword = Password.fromRaw("12345678");

        assertThatThrownBy(() ->
                User.create(
                        FullName.of("João Silva"),
                        Email.of("joao@example.com"),
                        rawPassword,
                        UserRole.USER
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password must be hashed");
    }

    @Test
    @DisplayName("Should reject raw password on reconstitution")
    void shouldRejectRawPasswordOnReconstitute() {
        var rawPassword = Password.fromRaw("12345678");

        assertThatThrownBy(() ->
                User.reconstitute(
                        UUID.randomUUID(),
                        FullName.of("João Silva"),
                        Email.of("joao@example.com"),
                        rawPassword,
                        UserRole.USER,
                        UserStatus.ACTIVE,
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password must be hashed");
    }

    @Test
    @DisplayName("Should deactivate an active user")
    void shouldDeactivateActiveUser() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        assertThat(user.isActive()).isTrue();

        user.pullEvents(); // clear creation event
        user.deactivate();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("Should activate an inactive user")
    void shouldActivateInactiveUser() {
        var user = User.reconstitute(
                UUID.randomUUID(),
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER,
                UserStatus.INACTIVE,
                null, null
        );

        user.activate();

        assertThat(user.isActive()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should suspend an active user")
    void shouldSuspendActiveUser() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        user.pullEvents(); // clear creation event
        user.suspend();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Should activate a suspended user")
    void shouldActivateSuspendedUser() {
        var user = User.reconstitute(
                UUID.randomUUID(),
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER,
                UserStatus.SUSPENDED,
                null, null
        );

        user.activate();

        assertThat(user.isActive()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw DomainException when activating already active user")
    void shouldThrowWhenActivatingActiveUser() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        assertThatThrownBy(user::activate)
                .isInstanceOf(DomainException.class)
                .hasMessage("user is already active");
    }

    @Test
    @DisplayName("Should throw DomainException when deactivating already inactive user")
    void shouldThrowWhenDeactivatingInactiveUser() {
        var user = User.reconstitute(
                UUID.randomUUID(),
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER,
                UserStatus.INACTIVE,
                null, null
        );

        assertThatThrownBy(user::deactivate)
                .isInstanceOf(DomainException.class)
                .hasMessage("user is already inactive");
    }

    @Test
    @DisplayName("Should throw DomainException when suspending inactive user")
    void shouldThrowWhenSuspendingInactiveUser() {
        var user = User.reconstitute(
                UUID.randomUUID(),
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER,
                UserStatus.INACTIVE,
                null, null
        );

        assertThatThrownBy(user::suspend)
                .isInstanceOf(DomainException.class)
                .hasMessage("cannot suspend an inactive user");
    }

    @Test
    @DisplayName("Should throw DomainException when deactivating suspended user")
    void shouldThrowWhenDeactivatingSuspendedUser() {
        var user = User.reconstitute(
                UUID.randomUUID(),
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER,
                UserStatus.SUSPENDED,
                null, null
        );

        assertThatThrownBy(user::deactivate)
                .isInstanceOf(DomainException.class)
                .hasMessage("cannot deactivate a suspended user — activate first");
    }

    @Test
    @DisplayName("Should throw DomainException when suspending already suspended user")
    void shouldThrowWhenSuspendingAlreadySuspendedUser() {
        var user = User.reconstitute(
                UUID.randomUUID(),
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER,
                UserStatus.SUSPENDED,
                null, null
        );

        assertThatThrownBy(user::suspend)
                .isInstanceOf(DomainException.class)
                .hasMessage("user is already suspended");
    }

    @Test
    @DisplayName("Should register UserDeactivatedEvent when deactivating")
    void shouldRegisterUserDeactivatedEvent() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        user.pullEvents(); // clear creation event
        user.deactivate();

        var events = user.pullEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(UserDeactivatedEvent.class);

        var event = (UserDeactivatedEvent) events.getFirst();
        assertThat(event.userId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should register UserActivatedEvent when activating")
    void shouldRegisterUserActivatedEvent() {
        var user = User.reconstitute(
                UUID.randomUUID(),
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER,
                UserStatus.INACTIVE,
                null, null
        );

        user.activate();

        var events = user.pullEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(UserActivatedEvent.class);

        var event = (UserActivatedEvent) events.getFirst();
        assertThat(event.userId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should register UserSuspendedEvent when suspending")
    void shouldRegisterUserSuspendedEvent() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        user.pullEvents(); // clear creation event
        user.suspend();

        var events = user.pullEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(UserSuspendedEvent.class);

        var event = (UserSuspendedEvent) events.getFirst();
        assertThat(event.userId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should promote user to admin")
    void shouldPromoteUser() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        user.promote();

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should keep admin role when promoting admin")
    void shouldKeepAdminRoleWhenPromotingAdmin() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.ADMIN
        );

        user.promote();

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should demote admin to user")
    void shouldDemoteAdmin() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.ADMIN
        );

        user.demote();

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should keep user role when demoting user")
    void shouldKeepUserRoleWhenDemotingUser() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$abcdefghijklmnopqrstuv"),
                UserRole.USER
        );

        user.demote();

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePassword() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$oldpasswordhash123456"),
                UserRole.USER
        );

        var newPassword = Password.fromHashed("$2a$10$newpasswordhash654321");

        user.changePassword(newPassword);

        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("Should reject raw password when changing password")
    void shouldRejectRawPasswordWhenChangingPassword() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$validhashedpassword"),
                UserRole.USER
        );

        assertThatThrownBy(() ->
                user.changePassword(Password.fromRaw("12345678"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password must be hashed");
    }

    @Test
    @DisplayName("Should change email successfully")
    void shouldChangeEmail() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("old@example.com"),
                Password.fromHashed("$2a$10$validhashedpassword"),
                UserRole.USER
        );

        var newEmail = Email.of("new@example.com");

        user.changeEmail(newEmail);

        assertThat(user.getEmail()).isEqualTo(newEmail);
    }

    @Test
    @DisplayName("Should rename user successfully")
    void shouldRenameUser() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$validhashedpassword"),
                UserRole.USER
        );

        var newName = FullName.of("Maria Silva");

        user.rename(newName);

        assertThat(user.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Should preserve entity identity after state changes")
    void shouldPreserveIdentityAfterMutations() {
        var user = User.create(
                FullName.of("João Silva"),
                Email.of("joao@example.com"),
                Password.fromHashed("$2a$10$validhashedpassword"),
                UserRole.USER
        );

        var originalId = user.getId();

        user.rename(FullName.of("Maria Silva"));
        user.changeEmail(Email.of("maria@example.com"));
        user.changePassword(Password.fromHashed("$2a$10$newhashpassword"));
        user.promote();
        user.deactivate();

        assertThat(user.getId()).isEqualTo(originalId);
    }
}
