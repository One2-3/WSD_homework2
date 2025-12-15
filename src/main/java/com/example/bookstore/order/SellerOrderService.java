package com.example.bookstore.order;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.order.dto.SellerOrderDtos.*;
import com.example.bookstore.user.User;
import com.example.bookstore.user.UserRepository;
import com.example.bookstore.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SellerOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public SellerOrderService(OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository,
                              UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<SellerOrderItemDto> list(Long sellerUserId, Pageable pageable) {
        Long sellerId = requireSellerId(sellerUserId);

        return orderItemRepository.findSellerOrderItems(sellerId, pageable)
                .map(row -> new SellerOrderItemDto(
                        row.getOrderId(),
                        row.getUserId(),
                        OrderStatus.valueOf(row.getStatus()),
                        row.getCreatedAt(),
                        row.getBookId(),
                        row.getQuantity(),
                        row.getUnitPriceCents(),
                        row.getSubtotalCents()
                ));
    }

    @Transactional(readOnly = true)
    public SellerOrderDetailDto detail(Long sellerUserId, Long orderId) {
        Long sellerId = requireSellerId(sellerUserId);

        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        List<OrderItem> myItems = orderItemRepository.findByOrderIdAndSellerId(orderId, sellerId);
        if (myItems.isEmpty()) {
            // 다른 판매자의 주문이거나 존재하지 않음을 동일하게 처리
            throw new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다.");
        }

        int sellerTotal = myItems.stream().mapToInt(OrderItem::getSubtotalCents).sum();

        List<SellerOrderItemLineDto> lines = myItems.stream()
                .map(oi -> new SellerOrderItemLineDto(
                        oi.getBookId(),
                        oi.getQuantity(),
                        oi.getUnitPriceCents(),
                        oi.getSubtotalCents()
                )).toList();

        return new SellerOrderDetailDto(
                o.getId(),
                o.getUserId(),
                o.getStatus(),
                o.getTotalAmountCents(),
                sellerTotal,
                o.getCreatedAt(),
                lines
        );
    }

    
    @Transactional
    public OrderStatus ship(Long sellerUserId, Long orderId) {
        Long sellerId = requireSellerId(sellerUserId);

        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        // 이 주문에 내 판매건이 있는지 확인
        List<OrderItem> myItems = orderItemRepository.findByOrderIdAndSellerId(orderId, sellerId);
        if (myItems.isEmpty()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다.");
        }

        if (o.getStatus() != OrderStatus.paid) {
            throw new ApiException(ErrorCode.CONFLICT, "paid 상태의 주문만 배송 처리할 수 있습니다.");
        }

        long distinctSellers = orderItemRepository.countDistinctSellersByOrderId(orderId);
        if (distinctSellers != 1L) {
            throw new ApiException(ErrorCode.CONFLICT, "복수 판매자 주문은 판매자 단독 배송처리를 지원하지 않습니다.(ADMIN으로 상태 변경)");
        }

        // 단일 판매자 주문이면 상태 변경 허용
        o.setStatus(OrderStatus.shipped);
        return o.getStatus();
    }

private Long requireSellerId(Long sellerUserId) {
        User me = userRepository.findByIdAndDeletedAtIsNull(sellerUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));

        if (me.getRole() != UserRole.seller || me.getSellerId() == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "판매자 권한이 없습니다.");
        }
        return me.getSellerId();
    }
}
