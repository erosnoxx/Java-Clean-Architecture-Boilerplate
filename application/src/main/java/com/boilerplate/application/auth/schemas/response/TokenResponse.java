package com.boilerplate.application.auth.schemas.response;

import java.time.Instant;

public record TokenResponse(
        String token,
        String refreshToken,
        Instant expiresAt
) { }
