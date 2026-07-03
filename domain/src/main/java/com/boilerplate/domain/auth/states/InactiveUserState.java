package com.boilerplate.domain.auth.states;

import com.boilerplate.domain.auth.enums.UserStatus;
import com.boilerplate.domain.auth.events.UserActivatedEvent;
import com.boilerplate.domain.auth.statemachine.UserStateMachine;
import com.boilerplate.domain.common.exceptions.DomainException;

public class InactiveUserState implements UserState {
    public static final InactiveUserState INSTANCE = new InactiveUserState();
    private InactiveUserState() {}

    @Override
    public void activate(UserStateMachine sm) {
        sm.transition(UserStatus.ACTIVE, new UserActivatedEvent(sm.getUserId()));
    }

    @Override
    public void deactivate(UserStateMachine sm) {
        throw new DomainException("user is already inactive");
    }

    @Override
    public void suspend(UserStateMachine sm) {
        throw new DomainException("cannot suspend an inactive user");
    }
}
