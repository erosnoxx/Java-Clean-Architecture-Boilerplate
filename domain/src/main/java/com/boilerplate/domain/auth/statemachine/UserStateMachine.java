package com.boilerplate.domain.auth.statemachine;

import com.boilerplate.domain.auth.entities.User;
import com.boilerplate.domain.auth.enums.UserStatus;
import com.boilerplate.domain.common.events.DomainEvent;
import com.boilerplate.domain.common.statemachine.StateMachine;

import java.util.UUID;

public class UserStateMachine extends StateMachine<UserStatus> {

    private final UUID userId;

    private UserStateMachine(UserStatus status, UUID userId) {
        this.value = status;
        this.userId = userId;
    }

    public static UserStateMachine of(User user) {
        return new UserStateMachine(user.getStatus(), user.getId());
    }

    public UUID getUserId() { return userId; }

    /** Called by state implementations to record the transition outcome. */
    public void transition(UserStatus newStatus, DomainEvent newEvent) {
        this.value = newStatus;
        this.event = newEvent;
    }

    public void activate() { value.getState().activate(this); }
    public void deactivate() { value.getState().deactivate(this); }
    public void suspend() { value.getState().suspend(this); }
}
