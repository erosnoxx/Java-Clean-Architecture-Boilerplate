package com.boilerplate.boot.config.beans.infra;

import com.boilerplate.infrastructure.auth.mappers.UserEntityMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfraMapperBeans {
    @Bean
    public UserEntityMapper userEntityMapper() {
        return new UserEntityMapper();
    }
}
