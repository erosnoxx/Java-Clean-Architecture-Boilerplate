package com.boilerplate.application.auth.schemas.request;

import com.boilerplate.domain.auth.enums.UserRole;
import com.boilerplate.domain.shared.vos.Email;
import com.boilerplate.domain.auth.vos.FullName;
import com.boilerplate.domain.auth.vos.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank String email,
        @NotBlank String password,
        @NotNull UserRole role
) {
    public RegisterRequest {
        FullName.of(name);
        Email.of(email);
        Password.fromRaw(password);
    }
}
