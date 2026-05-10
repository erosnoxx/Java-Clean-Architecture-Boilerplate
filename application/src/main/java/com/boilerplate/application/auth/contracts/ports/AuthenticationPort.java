package com.boilerplate.application.auth.contracts.ports;


import com.boilerplate.domain.auth.entities.User;

public interface AuthenticationPort {
    User authenticate(String email, String rawPassword);
}
