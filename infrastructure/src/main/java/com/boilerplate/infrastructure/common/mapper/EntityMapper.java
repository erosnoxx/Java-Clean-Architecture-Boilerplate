package com.boilerplate.infrastructure.common.mapper;

public interface EntityMapper<D, P> {
    P toPersistence(D domain);
    D toDomain(P persistence);
}
