package com.example.bookstore.common;

import java.util.List;

/**
 * 명세서: payload 안에는 items만 두고, meta는 최상위(meta)로 분리.
 */
public record ItemsPayload<T>(List<T> items) {}
