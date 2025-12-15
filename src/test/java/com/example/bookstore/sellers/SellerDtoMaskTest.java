package com.example.bookstore.sellers;

import com.example.bookstore.sellers.dto.SellerDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellerDtoMaskTest {

    private Seller sellerWithBusinessNo(String businessNo) {
        Seller s = new Seller();
        s.setName("S");
        s.setCommissionBps(0);
        s.setStatus(SellerStatus.active);
        s.setBusinessNo(businessNo);
        return s;
    }

    @Test
    void businessNoMasked_isNull_whenRawNull() {
        SellerDto dto = SellerDto.from(sellerWithBusinessNo(null));
        assertNull(dto.business_no_masked());
    }

    @Test
    void businessNoMasked_isNull_whenBlank() {
        SellerDto dto = SellerDto.from(sellerWithBusinessNo("   "));
        assertNull(dto.business_no_masked());
    }

    @Test
    void businessNoMasked_handlesShortDigits() {
        SellerDto dto = SellerDto.from(sellerWithBusinessNo("12-3"));
        assertEquals("*****", dto.business_no_masked());
    }

    @Test
    void businessNoMasked_keepsLastFiveDigits() {
        SellerDto dto = SellerDto.from(sellerWithBusinessNo("123-45-67890"));
        assertEquals("***-**-67890", dto.business_no_masked());
    }

    @Test
    void businessNoMasked_ignoresNonDigits() {
        SellerDto dto = SellerDto.from(sellerWithBusinessNo("AB12345CD67890"));
        assertEquals("***-**-67890", dto.business_no_masked());
    }

    @Test
    void bankAccountMasked_isPassedThrough() {
        Seller s = sellerWithBusinessNo("123-45-67890");
        s.setBankAccountMasked("***-**-*****-4567");
        SellerDto dto = SellerDto.from(s);
        assertEquals("***-**-*****-4567", dto.bank_account_masked());
    }
}
