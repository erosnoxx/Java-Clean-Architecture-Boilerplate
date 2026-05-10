package com.boilerplate.application.auth.contracts.ports;

public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
