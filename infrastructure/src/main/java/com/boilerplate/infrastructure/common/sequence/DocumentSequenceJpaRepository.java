package com.boilerplate.infrastructure.common.sequence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DocumentSequenceJpaRepository extends JpaRepository<DocumentSequenceEntity, DocumentSequenceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DocumentSequenceEntity s WHERE s.id.prefix = :prefix AND s.id.seqDate = :date")
    Optional<DocumentSequenceEntity> findByPrefixAndDateWithLock(
            @Param("prefix") String prefix,
            @Param("date") LocalDate date
    );
}
