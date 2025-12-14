package com.example.bookstore.common;

import org.springframework.data.domain.Page;

/**
 * 페이지네이션 메타: { page, limit, total, has_next }
 */
public record ApiMeta(int page, int limit, long total, boolean hasNext) {

    public static ApiMeta fromPage(Page<?> page) {
        return new ApiMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}
