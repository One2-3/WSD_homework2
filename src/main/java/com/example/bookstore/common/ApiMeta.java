package com.example.bookstore.common;

import org.springframework.data.domain.Page;

public record ApiMeta(int page, int limit, long total, boolean hasNext) {
    public static ApiMeta fromPage(Page<?> page) {
        return new ApiMeta(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.hasNext());
    }
}
