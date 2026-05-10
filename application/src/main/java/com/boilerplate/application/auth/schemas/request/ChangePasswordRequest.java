package com.boilerplate.application.auth.schemas.request;

import com.boilerplate.domain.auth.vos.Password;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank String newPassword
) {
    public ChangePasswordRequest {
        Password.fromRaw(newPassword);
    }
}