package com.example.bookstore.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

class PageableUtilTest {

    @Test
    void defaultPageAndLimit_whenNull() {
        PageRequest pr = PageableUtil.pageRequest(null, null);
        assertEquals(0, pr.getPageNumber());
        assertEquals(20, pr.getPageSize());
    }

    @Test
    void pageIsOneBaseInput() {
        PageRequest pr = PageableUtil.pageRequest(1, 10);
        assertEquals(0, pr.getPageNumber());
        assertEquals(10, pr.getPageSize());
    }

    @Test
    void pageLessThanOne_clampedToOne() {
        PageRequest pr = PageableUtil.pageRequest(0, 10);
        assertEquals(0, pr.getPageNumber());
    }

    @Test
    void limitLessThanOne_clampedToOne() {
        PageRequest pr = PageableUtil.pageRequest(1, 0);
        assertEquals(1, pr.getPageSize());
    }

    @Test
    void limitGreaterThanMax_clampedTo100() {
        PageRequest pr = PageableUtil.pageRequest(1, 1000);
        assertEquals(100, pr.getPageSize());
    }

    @Test
    void defaultSortIsIdDesc() {
        PageRequest pr = PageableUtil.pageRequest(1, 20);
        assertTrue(pr.getSort().isSorted());
        assertEquals("id", pr.getSort().iterator().next().getProperty());
        assertEquals(org.springframework.data.domain.Sort.Direction.DESC, pr.getSort().iterator().next().getDirection());
    }
}
