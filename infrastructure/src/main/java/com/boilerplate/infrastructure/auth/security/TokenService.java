package com.boilerplate.infrastructure.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.boilerplate.application.auth.schemas.response.TokenResponse;
import com.boilerplate.infrastructure.auth.data.entities.UserEntity;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor
public class TokenService {

    private final String secret;
    private final Long tokenExpiration;
    private final Long refreshExpiration;

    public TokenResponse generateToken(UserEntity user) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            var expiresAt = genExpirationDate();
            var refreshExpiresAt = genRefreshExpirationDate();

            var token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getUsername())
                    .withClaim("role", user.getRole().name())
                    .withExpiresAt(expiresAt)
                    .sign(algorithm);

            var refreshToken = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getUsername())
                    .withClaim("refresh", true)
                    .withExpiresAt(refreshExpiresAt)
                    .sign(algorithm);

            return new TokenResponse(token, refreshToken, expiresAt);

        } catch (JWTCreationException exception) {
            throw new RuntimeException("error while generating token", exception);
        }
    }

    public TokenResponse refreshToken(String refreshToken) {
        try {
            var algorithm = Algorithm.HMAC256(secret);

            var decoded = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .withClaim("refresh", true)
                    .build()
                    .verify(refreshToken);

            var username = decoded.getSubject();
            var expiresAt = genExpirationDate();

            var newToken = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(username)
                    .withExpiresAt(expiresAt)
                    .sign(algorithm);

            var newRefreshExpiresAt = genRefreshExpirationDate();

            var newRefreshToken = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(username)
                    .withClaim("refresh", true)
                    .withExpiresAt(newRefreshExpiresAt)
                    .sign(algorithm);

            return new TokenResponse(newToken, newRefreshToken, expiresAt);

        } catch (JWTVerificationException exception) {
            throw new RuntimeException("invalid or expired refresh token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            var algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now()
                .plusHours(tokenExpiration)
                .toInstant(ZoneOffset.UTC);
    }

    private Instant genRefreshExpirationDate() {
        return LocalDateTime.now()
                .plusHours(refreshExpiration)
                .toInstant(ZoneOffset.UTC);
    }
}