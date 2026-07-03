package com.boilerplate.domain.auth.states;

import com.boilerplate.domain.auth.enums.UserStatus;
import com.boilerplate.domain.auth.events.UserActivatedEvent;
import com.boilerplate.domain.auth.statemachine.UserStateMachine;
import com.boilerplate.domain.common.exceptions.DomainException;

public class SuspendedUserState implements UserState {
    public static final SuspendedUserState INSTANCE = new SuspendedUserState();
    private SuspendedUserState() {}

    @Override
    public void activate(UserStateMachine sm) {
        sm.transition(UserStatus.ACTIVE, new UserActivatedEvent(sm.getUserId()));
    }

    @Override
    public void deactivate(UserStateMachine sm) {
        throw new DomainException("cannot deactivate a suspended user — activate first");
    }

    @Override
    public void suspend(UserStateMachine sm) {
        throw new DomainException("user is already suspended");
    }
}
