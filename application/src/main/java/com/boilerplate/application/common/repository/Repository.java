package com.boilerplate.application.common.repository;

import com.boilerplate.application.common.pagination.*;
import com.boilerplate.domain.common.entities.DomainEntity;

import java.util.List;
import java.util.Optional;

public interface Repository<D extends DomainEntity<ID>, ID, C extends Criteria> {
    D save(D entity);
    List<D> saveBatch(Iterable<D> entities);
    D saveAndFlush(D entity);

    void flush();

    void delete(D entity);
    void deleteAll(Iterable<D> entities);
    void deleteAll();

    Optional<D> findById(ID id);
    boolean existsById(ID id);

    long count();
    long count(C criteria);

    Page<D> findAll(Pageable pageable);
    List<D> findAll();
    Page<D> findAll(C criteria, Pageable pageable);
}
