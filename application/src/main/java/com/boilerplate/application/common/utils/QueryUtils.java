package com.boilerplate.application.common.utils;

import com.boilerplate.application.common.repository.Repository;
import com.boilerplate.domain.common.entities.DomainEntity;
import com.boilerplate.domain.common.exceptions.NotFoundException;

public class QueryUtils {
    private QueryUtils() {}

    public static <D extends DomainEntity<ID>, ID> D findOrThrow(
            Repository<D, ID, ?> repository,
            ID id,
            String message
    ) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(message));
    }
}
