package com.mentoredu.config;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    private static final int MAX_PAGE_SIZE = 100;

    /** PageRequest con page ≥ 0 y size entre 1 y MAX_PAGE_SIZE. */
    public static PageRequest toPageRequest(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE));
    }

    public static <E, D> PagedResponse<D> from(Page<E> sourcePage, Function<E, D> mapper) {
        return new PagedResponse<>(
                sourcePage.getContent().stream().map(mapper).toList(),
                sourcePage.getNumber(),
                sourcePage.getSize(),
                sourcePage.getTotalElements(),
                sourcePage.getTotalPages(),
                sourcePage.isLast()
        );
    }
}
