package com.boilerplate.web.controllers;

import com.boilerplate.application.auth.contracts.usecases.auth.LoginUseCase;
import com.boilerplate.application.auth.contracts.usecases.auth.RefreshTokenUseCase;
import com.boilerplate.application.auth.contracts.usecases.auth.RegisterUseCase;
import com.boilerplate.application.auth.schemas.request.LoginRequest;
import com.boilerplate.application.auth.schemas.request.RefreshTokenRequest;
import com.boilerplate.application.auth.schemas.request.RegisterRequest;
import com.boilerplate.application.auth.schemas.response.TokenResponse;
import com.boilerplate.application.common.schemas.UUIDResponse;
import com.boilerplate.web.security.AdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController @RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginUseCase login;
    private final RegisterUseCase register;
    private final RefreshTokenUseCase refresh;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(this.login.execute(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(this.refresh.execute(request));
    }

    @PostMapping("/register") @AdminOnly
    public ResponseEntity<UUIDResponse> registerUser(
            @RequestBody @Valid RegisterRequest request) {
        var response = this.register.execute(request);
        return ResponseEntity.created(
                URI.create(
                        String.format("/users/%s", response.id())
                )).body(response);
    }
}
