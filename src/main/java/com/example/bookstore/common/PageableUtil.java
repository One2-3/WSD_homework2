package com.example.bookstore.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;

/**
 * 공통 규격
 * - page: 0-base
 * - size: 기본 20, 최대 100
 * - sort: field,ASC|DESC
 * - limit: 기존 호환용(Deprecated), size가 우선
 */
public final class PageableUtil {
    private PageableUtil() {}

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static PageRequest pageRequest(Integer page, Integer size, Integer limit, Sort sort) {
        int p = (page == null ? 0 : Math.max(page, 0));
        Integer raw = (size != null ? size : limit);
        int s = (raw == null ? DEFAULT_SIZE : Math.min(Math.max(raw, 1), MAX_SIZE));
        Sort realSort = (sort == null ? Sort.by(Sort.Direction.DESC, "id") : sort);
        return PageRequest.of(p, s, realSort);
    }
    public static PageRequest pageRequest(Integer page, Integer size, Integer limit) {
        return pageRequest(page, size, limit, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
    }
    public static PageRequest pageRequest(Integer page, Integer limit) {
        return pageRequest(page, null, limit);
    }
    
    public static Sort parseSort(String sortParam,
                                 Sort defaultSort,
                                 Set<String> allowedFields,
                                 Map<String, String> aliases) {
        if (sortParam == null || sortParam.isBlank()) return defaultSort;

        if (!sortParam.contains(",")) {
            // 레거시 키워드 지원
            return switch (sortParam) {
                case "latest" -> defaultSort;
                case "oldest" -> defaultSort.ascending();
                default -> throw new ApiException(
                        ErrorCode.INVALID_QUERY_PARAM,
                        "sort 형식이 올바르지 않습니다.",
                        Map.of("sort", "expected field,ASC|DESC")
                );
            };
        }

        String[] parts = sortParam.split(",");
        if (parts.length != 2) {
            throw new ApiException(ErrorCode.INVALID_QUERY_PARAM, "sort 형식이 올바르지 않습니다.",
                    Map.of("sort", "expected field,ASC|DESC"));
        }

        String field = parts[0].trim();
        String dirRaw = parts[1].trim().toUpperCase();

        if (aliases != null && aliases.containsKey(field)) field = aliases.get(field);
        if (allowedFields != null && !allowedFields.isEmpty() && !allowedFields.contains(field)) {
            throw new ApiException(ErrorCode.INVALID_QUERY_PARAM, "허용되지 않는 정렬 필드입니다.",
                    Map.of("sort", "allowed=" + allowedFields));
        }

        Sort.Direction dir;
        try { dir = Sort.Direction.valueOf(dirRaw); }
        catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_QUERY_PARAM, "sort 방향이 올바르지 않습니다.",
                    Map.of("sort", "ASC|DESC"));
        }

        return Sort.by(dir, field);
    }
}
