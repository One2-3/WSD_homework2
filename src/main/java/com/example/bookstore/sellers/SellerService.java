package com.example.bookstore.sellers;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.sellers.dto.SellerCreateRequest;
import com.example.bookstore.sellers.dto.SellerPatchRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class SellerService {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    /** 6-1 판매자 생성 */
    public Seller create(SellerCreateRequest req) {
        Seller s = new Seller();
        s.setName(req.name());
        s.setCommissionBps(req.commission_bps());
        s.setStatus(req.status() == null ? SellerStatus.active : req.status());
        s.setContactEmail(req.contact_email());
        s.setPhone(req.phone());
        s.setAddress(req.address());
        s.setBusinessNo(req.business_no()); // 서버 저장 평문 OK (응답은 마스킹)
        s.setBankName(req.bank_name());
        // bank_account는 입력 전용 → 마스킹해서 bank_account_masked에만 저장
        s.setBankAccountMasked(maskBankAccount(req.bank_account()));

        return sellerRepository.save(s);
    }

    /** 6-2 판매자 목록 */
    @Transactional(readOnly = true)
    public Page<Seller> list(SellerStatus status, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.DESC, "id"));
        if (status == null) {
            return sellerRepository.findAllByDeletedAtIsNull(pageable);
        }
        return sellerRepository.findAllByDeletedAtIsNullAndStatus(pageable, status);
    }

    /** 6-3 판매자 상세 */
    @Transactional(readOnly = true)
    public Seller get(long sellerId) {
        return sellerRepository.findByIdAndDeletedAtIsNull(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Seller not found"));
    }

    /** 6-4 판매자 수정: status/commission_bps/name만 */
    public Seller patch(long sellerId, SellerPatchRequest req) {
        Seller s = get(sellerId);

        if (req.status() != null) s.setStatus(req.status());
        if (req.commission_bps() != null) s.setCommissionBps(req.commission_bps());
        if (req.name() != null && !req.name().isBlank()) s.setName(req.name());

        return sellerRepository.save(s);
    }

    /** 6-5 판매자 삭제(소프트) */
    public void softDelete(long sellerId) {
        Seller s = get(sellerId);
        s.setDeletedAt(Instant.now());
        sellerRepository.save(s);
    }

    /** 6-6 판매자 삭제(하드) */
    public void hardDelete(long sellerId) {
        Seller s = get(sellerId);
        try {
            sellerRepository.delete(s);
        } catch (DataIntegrityViolationException e) {
            // 책/주문/정산 연계 시 RESTRICT 가능 → 409로 처리하는 게 안전
            throw new ApiException(ErrorCode.CONFLICT, "Seller has related data. Cannot hard delete.");
        }
    }

    // bank_account_masked: "***-**-*****-4567" 형태 예시
    private String maskBankAccount(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return "***-**-*****-****";
        String last4 = digits.substring(digits.length() - 4);
        return "***-**-*****-" + last4;
    }
}
