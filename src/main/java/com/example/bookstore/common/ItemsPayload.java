package com.example.bookstore.common;

import java.util.List;

/**
 * 목록 payload는 items[]를 권장.
 */
public record ItemsPayload<T>(List<T> items) {
}
