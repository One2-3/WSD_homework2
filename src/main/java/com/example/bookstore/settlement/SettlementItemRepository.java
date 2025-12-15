
package com.example.bookstore.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    List<SettlementItem> findBySettlementId(Long settlementId);

    List<SettlementItem> findBySettlementIdAndSellerId(Long settlementId, Long sellerId);
}
