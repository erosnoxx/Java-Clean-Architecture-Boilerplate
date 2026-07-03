package com.boilerplate.boot.config.beans.infra;

import com.boilerplate.application.common.sequence.DocumentSequencePort;
import com.boilerplate.infrastructure.common.sequence.DocumentSequenceAdapter;
import com.boilerplate.infrastructure.common.sequence.DocumentSequenceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InfraSequenceBeans {

    private final DocumentSequenceJpaRepository documentSequenceJpaRepository;

    @Bean
    public DocumentSequencePort documentSequencePort() {
        return new DocumentSequenceAdapter(documentSequenceJpaRepository);
    }
}
