package com.boilerplate.domain.common.entities;

import com.boilerplate.domain.common.events.DomainEvent;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public abstract class DomainEntity<ID> {
    private ID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private final List<DomainEvent> events = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        events.add(event);
    }

    public List<DomainEvent> pullEvents() {
        var pending = List.copyOf(events);
        events.clear();
        return pending;
    }
}
