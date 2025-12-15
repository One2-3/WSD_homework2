package com.example.bookstore.review;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class ReviewSort {
    private ReviewSort() {}

    // 기존 호환 (page, limit, sort)
    public static Pageable pageable(Integer page, Integer limit, String sort) {
        int p = (page == null ? 1 : Math.max(page, 1)) - 1;
        int l = (limit == null ? 20 : Math.min(Math.max(limit, 1), 100));

        Sort s;
        String key = (sort == null ? "latest" : sort);
        switch (key) {
            case "rating_desc" -> s = Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "id"));
            case "rating_asc" -> s = Sort.by(Sort.Direction.ASC, "rating").and(Sort.by(Sort.Direction.DESC, "id"));
            default -> s = Sort.by(Sort.Direction.DESC, "id"); // latest
        }
        return PageRequest.of(p, l, s);
    }

    // ✅ 너가 컨트롤러에서 쓰는 형태(page,size,limit,sort) 지원
    public static Pageable pageable(Integer page, Integer size, Integer limit, String sort) {
        Integer effective = (size != null ? size : limit);
        return pageable(page, effective, sort);
    }
}
