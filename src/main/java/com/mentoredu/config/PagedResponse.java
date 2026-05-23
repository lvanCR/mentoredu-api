package com.mentoredu.config;

import org.springframework.data.domain.Page;

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
