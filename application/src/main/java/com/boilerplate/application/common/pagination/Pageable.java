package com.boilerplate.application.common.pagination;

public record Pageable(
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    public static Pageable of(int page, int size) {
        return new Pageable(page, size, null, null);
    }

    public static Pageable of(int page, int size, String sortBy, String sortDirection) {
        return new Pageable(page, size, sortBy, sortDirection);
    }

    public int getOffset() {
        return page * size;
    }

    public int getPageSize() {
        return size;
    }
}