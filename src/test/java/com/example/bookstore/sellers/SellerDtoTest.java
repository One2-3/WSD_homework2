package com.example.bookstore.sellers;

import com.example.bookstore.sellers.dto.SellerDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellerDtoTest {

    @Test
    void businessNoIsMasked() {
        Seller s = new Seller();
        s.setName("A");
        s.setCommissionBps(0);
        s.setStatus(SellerStatus.active);
        s.setBusinessNo("123-45-67890");
        SellerDto dto = SellerDto.from(s);
        assertEquals("***-**-67890", dto.business_no_masked());
    }

    @Test
    void businessNoNull_staysNull() {
        Seller s = new Seller();
        s.setName("A");
        s.setCommissionBps(0);
        s.setStatus(SellerStatus.active);
        s.setBusinessNo(null);
        SellerDto dto = SellerDto.from(s);
        assertNull(dto.business_no_masked());
    }

    @Test
    void bankAccountMasked_passThroughEntityValue() {
        Seller s = new Seller();
        s.setName("A");
        s.setCommissionBps(0);
        s.setStatus(SellerStatus.active);
        s.setBankAccountMasked("***-**-*****-9999");
        SellerDto dto = SellerDto.from(s);
        assertEquals("***-**-*****-9999", dto.bank_account_masked());
    }

    @Test
    void idCanBeNull_beforePersist() {
        Seller s = new Seller();
        s.setName("A");
        s.setCommissionBps(0);
        s.setStatus(SellerStatus.active);
        SellerDto dto = SellerDto.from(s);
        assertNull(dto.id());
    }

    @Test
    void statusIsCopied() {
        Seller s = new Seller();
        s.setName("A");
        s.setCommissionBps(0);
        s.setStatus(SellerStatus.suspended);
        SellerDto dto = SellerDto.from(s);
        assertEquals(SellerStatus.suspended, dto.status());
    }
}
