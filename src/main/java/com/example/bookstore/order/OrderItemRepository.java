package com.example.bookstore.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByOrderIdAndSellerId(Long orderId, Long sellerId);

    interface SellerOrderItemRow {
        Long getOrderId();
        Long getUserId();
        String getStatus();
        Instant getCreatedAt();
        Long getBookId();
        Integer getQuantity();
        Integer getUnitPriceCents();
        Integer getSubtotalCents();
    }

    @Query(
            value = """
                    select oi.order_id          as orderId,
                           o.user_id           as userId,
                           o.status            as status,
                           o.created_at        as createdAt,
                           oi.book_id          as bookId,
                           oi.quantity         as quantity,
                           oi.unit_price_cents as unitPriceCents,
                           oi.subtotal_cents   as subtotalCents
                      from order_items oi
                      join orders o on o.id = oi.order_id
                     where oi.seller_id = :sellerId
                     order by oi.id desc
                    """,
            countQuery = """
                    select count(*)
                      from order_items oi
                     where oi.seller_id = :sellerId
                    """,
            nativeQuery = true
    )
    Page<SellerOrderItemRow> findSellerOrderItems(@Param("sellerId") Long sellerId, Pageable pageable);


    @Query(value = "select count(distinct seller_id) from order_items where order_id = :orderId", nativeQuery = true)
    long countDistinctSellersByOrderId(@Param("orderId") Long orderId);


}
