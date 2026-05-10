package com.boilerplate.application.common.pagination;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public record Page<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) implements Iterable<T> {

    public <R> Page<R> map(Function<T, R> mapper) {
        return new Page<>(
                content.stream().map(mapper).toList(),
                page,
                size,
                totalElements,
                totalPages
        );
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}