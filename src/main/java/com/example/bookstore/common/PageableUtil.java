package com.example.bookstore.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageableUtil {
    private PageableUtil() {}

    /**
     * 과제 요구사항 기준 파라미터: page, size
     * - 기존 구현의 limit도 호환을 위해 유지 (size가 우선).
     * - 입력 page는 1-base로 받고 내부적으로 0-base로 변환.
     */
    public static PageRequest pageRequest(Integer page, Integer size, Integer limit) {
        int p = (page == null ? 1 : Math.max(page, 1)) - 1;     // 1-base input
        Integer raw = (size != null ? size : limit);
        int l = (raw == null ? 20 : Math.min(Math.max(raw, 1), 100));
        return PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * 기존 코드 호환용 (page, limit)
     */
    public static PageRequest pageRequest(Integer page, Integer limit) {
        return pageRequest(page, null, limit);
    }
}
