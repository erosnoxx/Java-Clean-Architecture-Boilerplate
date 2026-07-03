package com.boilerplate.infrastructure.common.sequence;

import com.boilerplate.application.common.sequence.DocumentSequencePort;
import com.boilerplate.infrastructure.common.utils.TimeConfig;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DocumentSequenceAdapter implements DocumentSequencePort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    private final DocumentSequenceJpaRepository repository;

    public DocumentSequenceAdapter(DocumentSequenceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public String next(String prefix) {
        var today = LocalDate.now(TimeConfig.DEFAULT_OFFSET);
        var entity = repository.findByPrefixAndDateWithLock(prefix, today)
                .orElseGet(() -> {
                    var newEntity = new DocumentSequenceEntity();
                    newEntity.setId(new DocumentSequenceId(prefix, today));
                    newEntity.setLastSeq(0);
                    return newEntity;
                });

        entity.setLastSeq(entity.getLastSeq() + 1);
        repository.save(entity);

        return prefix + "-" + today.format(DATE_FORMAT) + "-" + String.format("%04d", entity.getLastSeq());
    }
}
