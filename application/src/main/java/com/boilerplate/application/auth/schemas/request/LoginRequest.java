package com.boilerplate.application.auth.schemas.request;

import com.boilerplate.domain.shared.vos.Email;
import com.boilerplate.domain.auth.vos.Password;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {
    public LoginRequest {
        Email.of(email);
        Password.fromRaw(password);
    }

}
