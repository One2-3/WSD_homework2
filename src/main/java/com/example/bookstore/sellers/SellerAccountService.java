package com.example.bookstore.sellers;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.sellers.dto.SellerAccountCreateRequest;
import com.example.bookstore.user.User;
import com.example.bookstore.user.UserRepository;
import com.example.bookstore.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매자(seller) 로그인 계정 생성/관리 서비스.
 * - ADMIN만 호출하도록 컨트롤러에서 PreAuthorize 걸어둠.
 */
@Service
public class SellerAccountService {

    private final SellerService sellerService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SellerAccountService(SellerService sellerService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.sellerService = sellerService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ADMIN: sellerId에 연결된 로그인 계정 생성
     */
    @Transactional
    public User createSellerAccount(long sellerId, SellerAccountCreateRequest req) {
        Seller seller = sellerService.get(sellerId);

        if (userRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
            throw new ApiException(ErrorCode.CONFLICT, "이메일이 이미 존재합니다.");
        }

        User u = new User();
        u.setEmail(req.email());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setName((req.name() == null || req.name().isBlank()) ? seller.getName() : req.name());
        u.setRole(UserRole.seller);
        u.setSellerId(sellerId);

        return userRepository.save(u);
    }
}
