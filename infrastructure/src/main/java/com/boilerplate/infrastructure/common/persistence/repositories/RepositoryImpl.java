package com.boilerplate.infrastructure.common.persistence.repositories;

import com.boilerplate.application.common.pagination.*;
import com.boilerplate.application.common.repository.Criteria;
import com.boilerplate.application.common.repository.Repository;
import com.boilerplate.domain.common.entities.DomainEntity;
import com.boilerplate.infrastructure.common.mapper.EntityMapper;
import com.boilerplate.infrastructure.common.persistence.entities.PersistenceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositoryImpl<
        D extends DomainEntity<ID>, ID, C extends Criteria,
        P extends PersistenceEntity<ID>, R extends JpaRepository<P, ID>>
            implements Repository<D, ID, C> {

    protected final R jpaRepository;
    protected final EntityManager em;
    protected final EntityMapper<D, P> mapper;
    protected final Class<P> entityClass;

    protected RepositoryImpl(
            R jpaRepository,
            EntityManager em,
            EntityMapper<D, P> mapper,
            Class<P> entityClass
    ) {
        this.jpaRepository = jpaRepository;
        this.em = em;
        this.mapper = mapper;
        this.entityClass = entityClass;
    }

    @Override
    public D save(D entity) {
        var saved = jpaRepository.save(mapper.toPersistence(entity));
        return mapper.toDomain(saved);
    }

    @Override
    public List<D> saveBatch(Iterable<D> entities) {
        List<P> toSave = new ArrayList<>();
        entities.forEach(e -> toSave.add(mapper.toPersistence(e)));

        return jpaRepository.saveAll(toSave).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public D saveAndFlush(D entity) {
        var saved = jpaRepository.saveAndFlush(mapper.toPersistence(entity));
        return mapper.toDomain(saved);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public void delete(D entity) {
        jpaRepository.delete(mapper.toPersistence(entity));
    }

    @Override
    public void deleteAll(Iterable<D> entities) {
        List<P> toDelete = new ArrayList<>();
        entities.forEach(e -> toDelete.add(mapper.toPersistence(e)));
        jpaRepository.deleteAll(toDelete);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public Optional<D> findById(ID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(ID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long count(C criteria) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Long.class);
        var root = query.from(entityClass);

        query.select(cb.count(root))
                .where(CriteriaUtils.buildPredicates(criteria, root, cb));

        return em.createQuery(query).getSingleResult();
    }

    @Override
    public Page<D> findAll(Pageable pageable) {
        var springPage = jpaRepository.findAll(PageableAdapter.toSpring(pageable));
        return PageableAdapter.toDomain(springPage.map(mapper::toDomain));
    }

    @Override
    public List<D> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<D> findAll(C criteria, Pageable pageable) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(entityClass);
        var root = query.from(entityClass);

        query.where(CriteriaUtils.buildPredicates(criteria, root, cb));
        applySorting(query, root, cb, pageable);

        var typedQuery = em.createQuery(query);
        typedQuery.setFirstResult(pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<D> content = typedQuery.getResultList().stream()
                .map(mapper::toDomain)
                .toList();

        long total = count(criteria);
        int totalPages = (int) Math.ceil((double) total / pageable.size());

        return new Page<>(content, pageable.page(), pageable.size(), total, totalPages);
    }

    private void applySorting(CriteriaQuery<?> query, Root<?> root, CriteriaBuilder cb, Pageable pageable) {
        if (pageable.sortBy() == null) return;
        var order = "DESC".equalsIgnoreCase(pageable.sortDirection())
                ? cb.desc(root.get(pageable.sortBy()))
                : cb.asc(root.get(pageable.sortBy()));
        query.orderBy(order);
    }
}
