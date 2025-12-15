package com.example.bookstore.sellers;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.sellers.dto.SellerSelfPatchRequest;
import com.example.bookstore.user.User;
import com.example.bookstore.user.UserRepository;
import com.example.bookstore.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerSelfService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    public SellerSelfService(SellerRepository sellerRepository, UserRepository userRepository) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Seller getSelf(Long sellerUserId) {
        Long sellerId = requireSellerId(sellerUserId);
        return sellerRepository.findByIdAndDeletedAtIsNull(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "판매자를 찾을 수 없습니다."));
    }

    @Transactional
    public Seller patchSelf(Long sellerUserId, SellerSelfPatchRequest req) {
        Seller s = getSelf(sellerUserId);

        if (req.contact_email() != null) s.setContactEmail(req.contact_email());
        if (req.phone() != null) s.setPhone(req.phone());
        if (req.address() != null) s.setAddress(req.address());
        if (req.bank_name() != null) s.setBankName(req.bank_name());
        if (req.bank_account() != null) {
            s.setBankAccountMasked(maskBankAccount(req.bank_account()));
        }

        return sellerRepository.save(s);
    }

    private Long requireSellerId(Long sellerUserId) {
        User me = userRepository.findByIdAndDeletedAtIsNull(sellerUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));

        if (me.getRole() != UserRole.seller || me.getSellerId() == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "판매자 권한이 없습니다.");
        }
        return me.getSellerId();
    }

    // "***-**-*****-4567" 형태 예시
    private String maskBankAccount(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return "***-**-*****-****";
        String last4 = digits.substring(digits.length() - 4);
        return "***-**-*****-" + last4;
    }
}
