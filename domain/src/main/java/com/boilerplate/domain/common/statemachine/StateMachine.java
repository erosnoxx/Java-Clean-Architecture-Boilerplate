package com.boilerplate.domain.common.statemachine;

import com.boilerplate.domain.common.events.DomainEvent;

public abstract class StateMachine<V extends Enum<V>> {
    protected V value;
    protected DomainEvent event;

    public V getValue() { return value; }
    public DomainEvent getEvent() { return event; }
}
