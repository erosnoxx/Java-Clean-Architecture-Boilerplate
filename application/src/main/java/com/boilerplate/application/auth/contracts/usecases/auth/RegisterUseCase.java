package com.boilerplate.application.auth.contracts.usecases.auth;

import com.boilerplate.application.auth.schemas.request.RegisterRequest;
import com.boilerplate.application.common.schemas.UUIDResponse;

public interface RegisterUseCase {
    UUIDResponse execute(RegisterRequest request);
}
