package com.example.bookstore.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

class PageableUtilExtraTest {

    @Test
    void pageNull_defaultsToFirstPage() {
        PageRequest pr = PageableUtil.pageRequest(null, 20);
        assertEquals(0, pr.getPageNumber());
    }

    @Test
    void limitNull_defaultsTo20() {
        PageRequest pr = PageableUtil.pageRequest(1, null);
        assertEquals(20, pr.getPageSize());
    }

    @Test
    void pageIsOneBased() {
        PageRequest pr = PageableUtil.pageRequest(2, 20);
        assertEquals(1, pr.getPageNumber());
    }

    @Test
    void limitIsClampedToMax100() {
        PageRequest pr = PageableUtil.pageRequest(1, 1000);
        assertEquals(100, pr.getPageSize());
    }

    @Test
    void limitIsClampedToMin1() {
        PageRequest pr = PageableUtil.pageRequest(1, 0);
        assertEquals(1, pr.getPageSize());
    }
}
