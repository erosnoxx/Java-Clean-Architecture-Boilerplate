package com.boilerplate.boot.config.security;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SecurityMatchers {
    public static RequestMatcher[] getPublicEndpoints() {
        return new RequestMatcher[] {
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/auth/login")
        };
    }
}
