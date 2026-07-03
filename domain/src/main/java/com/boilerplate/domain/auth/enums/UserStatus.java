package com.boilerplate.domain.auth.enums;

import com.boilerplate.domain.auth.states.UserState;
import com.boilerplate.domain.auth.states.ActiveUserState;
import com.boilerplate.domain.auth.states.InactiveUserState;
import com.boilerplate.domain.auth.states.SuspendedUserState;

public enum UserStatus {
    ACTIVE {
        @Override public UserState getState() { return ActiveUserState.INSTANCE; }
    },
    INACTIVE {
        @Override public UserState getState() { return InactiveUserState.INSTANCE; }
    },
    SUSPENDED {
        @Override public UserState getState() { return SuspendedUserState.INSTANCE; }
    };

    public abstract UserState getState();
}
