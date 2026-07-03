package com.boilerplate.domain.common.entities;

import com.boilerplate.domain.common.events.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class DomainEntity<ID> {
    @Setter(AccessLevel.PROTECTED)
    private ID id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private final List<DomainEvent> events = new ArrayList<>();

    protected void setTimestamps(OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected void registerEvent(DomainEvent event) {
        events.add(event);
    }

    public List<DomainEvent> pullEvents() {
        var pending = List.copyOf(events);
        events.clear();
        return pending;
    }
}
