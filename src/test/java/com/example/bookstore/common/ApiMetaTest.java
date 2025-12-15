package com.example.bookstore.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiMetaTest {

    @Test
    void fromPage_isOneBasePage() {
        var page = new PageImpl<>(List.of(1,2,3), PageRequest.of(0, 20), 3);
        ApiMeta meta = ApiMeta.fromPage(page);
        assertEquals(1, meta.page());
        assertEquals(20, meta.limit());
        assertEquals(3, meta.total());
        assertFalse(meta.hasNext());
    }

    @Test
    void fromPage_hasNextTrueWhenMorePages() {
        var page = new PageImpl<>(List.of(1,2,3), PageRequest.of(0, 2), 5);
        ApiMeta meta = ApiMeta.fromPage(page);
        assertTrue(meta.hasNext());
    }

    @Test
    void fromPage_pageTwo() {
        var page = new PageImpl<>(List.of(1,2), PageRequest.of(1, 2), 10);
        ApiMeta meta = ApiMeta.fromPage(page);
        assertEquals(2, meta.page());
    }

    @Test
    void fromPage_limitMatchesSize() {
        var page = new PageImpl<>(List.of(1), PageRequest.of(0, 7), 100);
        ApiMeta meta = ApiMeta.fromPage(page);
        assertEquals(7, meta.limit());
    }

    @Test
    void fromPage_totalMatchesTotalElements() {
        var page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 999);
        ApiMeta meta = ApiMeta.fromPage(page);
        assertEquals(999, meta.total());
    }
}
