package com.boilerplate.boot.config.beans.infra;

import com.boilerplate.infrastructure.auth.listeners.UserEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfraListenerBeans {

    @Bean
    public UserEventListener userEventListener() {
        return new UserEventListener();
    }
}
