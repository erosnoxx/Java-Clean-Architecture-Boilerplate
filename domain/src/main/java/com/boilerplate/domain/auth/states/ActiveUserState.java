package com.boilerplate.domain.auth.states;

import com.boilerplate.domain.auth.enums.UserStatus;
import com.boilerplate.domain.auth.events.UserDeactivatedEvent;
import com.boilerplate.domain.auth.events.UserSuspendedEvent;
import com.boilerplate.domain.auth.statemachine.UserStateMachine;
import com.boilerplate.domain.common.exceptions.DomainException;

public class ActiveUserState implements UserState {
    public static final ActiveUserState INSTANCE = new ActiveUserState();
    private ActiveUserState() {}

    @Override
    public void activate(UserStateMachine sm) {
        throw new DomainException("user is already active");
    }

    @Override
    public void deactivate(UserStateMachine sm) {
        sm.transition(UserStatus.INACTIVE, new UserDeactivatedEvent(sm.getUserId()));
    }

    @Override
    public void suspend(UserStateMachine sm) {
        sm.transition(UserStatus.SUSPENDED, new UserSuspendedEvent(sm.getUserId()));
    }
}
