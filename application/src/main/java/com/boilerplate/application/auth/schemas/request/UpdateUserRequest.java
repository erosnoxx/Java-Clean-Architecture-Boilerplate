package com.boilerplate.application.auth.schemas.request;

import com.boilerplate.domain.auth.vos.FullName;
import com.boilerplate.domain.shared.vos.Email;
import java.util.Optional;

public record UpdateUserRequest(
        Optional<String> name,
        Optional<String> email
) {
    public UpdateUserRequest {
        name.ifPresent(FullName::of);
        email.ifPresent(Email::of);
    }
}
