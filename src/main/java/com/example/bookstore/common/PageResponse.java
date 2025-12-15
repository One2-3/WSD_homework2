package com.example.bookstore.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort
) {
    public static <T> PageResponse<T> from(Page<T> pageObj, Pageable pageable) {
        return new PageResponse<>(
                pageObj.getContent(),
                pageObj.getNumber(),
                pageObj.getSize(),
                pageObj.getTotalElements(),
                pageObj.getTotalPages(),
                sortToString(pageable.getSort())
        );
    }

    public static <T> PageResponse<T> fromList(List<T> items) {
        int size = (items == null ? 0 : items.size());
        int totalPages = (size == 0 ? 0 : 1);
        return new PageResponse<>(items, 0, size, size, totalPages, null);
    }

    public static String sortToString(Sort sort) {
        if (sort == null || sort.isUnsorted()) return null;
        return sort.stream()
                .map(o -> o.getProperty() + "," + o.getDirection().name())
                .reduce((a, b) -> a + ";" + b)
                .orElse(null);
    }
}
