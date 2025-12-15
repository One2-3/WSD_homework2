
package com.example.bookstore.settlement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Page<Settlement> findByStatus(SettlementStatus status, Pageable pageable);

    Page<Settlement> findBySellerId(Long sellerId, Pageable pageable);

    Page<Settlement> findBySellerIdAndStatus(Long sellerId, SettlementStatus status, Pageable pageable);
}
