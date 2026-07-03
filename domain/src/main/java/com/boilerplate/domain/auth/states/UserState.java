package com.boilerplate.domain.auth.states;

import com.boilerplate.domain.auth.statemachine.UserStateMachine;

public interface UserState {
    void activate(UserStateMachine sm);
    void deactivate(UserStateMachine sm);
    void suspend(UserStateMachine sm);
}
