package com.boilerplate.application.common.schemas;

import java.util.UUID;

public record UUIDResponse(UUID id) {
    public static UUIDResponse of(UUID id) {
        return new UUIDResponse(id);
    }
}
