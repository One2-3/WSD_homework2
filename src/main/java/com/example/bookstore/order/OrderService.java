package com.example.bookstore.order;

import com.example.bookstore.book.BookRepository;
import com.example.bookstore.cart.CartItem;
import com.example.bookstore.cart.CartService;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.order.dto.OrderDtos.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        BookRepository bookRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookRepository = bookRepository;
        this.cartService = cartService;
    }

    @Transactional
    public CreateOrderResponse create(Long userId, List<CreateItem> items) {
        if (items == null || items.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "items는 비어 있을 수 없습니다.");
        }

        // 1) 재고 차감(낙관적: update where stock>=qty)
        for (CreateItem it : items) {
            int updated = bookRepository.decreaseStockIfEnough(it.book_id(), it.quantity());
            if (updated == 0) throw new ApiException(ErrorCode.VALIDATION_FAILED, "재고가 부족합니다.");
        }

        // 2) 주문 생성
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.pending);

        int total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateItem it : items) {
            var snap = bookRepository.getForCartOrder(it.book_id())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

            int unit = snap.getPriceCents();
            int subtotal = unit * it.quantity();
            total += subtotal;

            OrderItem oi = new OrderItem();
            oi.setBookId(it.book_id());
            oi.setSellerId(snap.getSellerId());
            oi.setQuantity(it.quantity());
            oi.setUnitPriceCents(unit);
            oi.setSubtotalCents(subtotal);
            orderItems.add(oi);
        }

        order.setTotalAmountCents(total);
        Order savedOrder = orderRepository.save(order);

        for (OrderItem oi : orderItems) {
            oi.setOrderId(savedOrder.getId());
        }
        orderItemRepository.saveAll(orderItems);

        return new CreateOrderResponse(savedOrder.getId(), savedOrder.getCreatedAt());
    }

    /**
     * 장바구니 기반 결제/주문 생성
     * - cart에 있는 항목으로 주문 생성 후 cart 비움
     */
    @Transactional
    public CreateOrderResponse createFromCart(Long userId) {
        List<CartItem> cartItems = cartService.listItemsForCheckout(userId);
        if (cartItems.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "장바구니가 비어 있습니다.");
        }

        List<CreateItem> items = cartItems.stream()
                .map(ci -> new CreateItem(ci.getBookId(), ci.getQuantity()))
                .toList();

        CreateOrderResponse resp = create(userId, items);

        // 주문 생성 성공 시 cart 비우기
        cartService.clear(userId);

        return resp;
    }

    @Transactional
    public Page<OrderSummaryDto> listMy(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByIdDesc(userId, pageable)
                .map(o -> new OrderSummaryDto(o.getId(), o.getStatus(), o.getTotalAmountCents(), o.getCreatedAt()));
    }

    @Transactional
    public OrderDetailDto detailMy(Long userId, Long orderId) {
        Order o = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        List<OrderDetailItemDto> items = orderItemRepository.findByOrderId(o.getId()).stream()
                .map(oi -> new OrderDetailItemDto(
                        oi.getBookId(), oi.getSellerId(), oi.getQuantity(), oi.getUnitPriceCents(), oi.getSubtotalCents()
                )).toList();

        return new OrderDetailDto(o.getId(), o.getStatus(), o.getTotalAmountCents(), o.getCreatedAt(), items);
    }
    
    @Transactional
    public OrderSummaryDto payMy(Long userId, Long orderId) {
        Order o = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (o.getStatus() == OrderStatus.paid) {
            return new OrderSummaryDto(o.getId(), o.getStatus(), o.getTotalAmountCents(), o.getCreatedAt());
        }
        if (o.getStatus() != OrderStatus.pending) {
            throw new ApiException(ErrorCode.CONFLICT, "pending 상태의 주문만 결제할 수 있습니다.");
        }
        o.setStatus(OrderStatus.paid);
        return new OrderSummaryDto(o.getId(), o.getStatus(), o.getTotalAmountCents(), o.getCreatedAt());
    }

public AdminPatchStatusResponse adminPatchStatus(Long orderId, OrderStatus newStatus) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        OrderStatus prev = o.getStatus();
        o.setStatus(newStatus);

        return new AdminPatchStatusResponse(o.getId(), prev, o.getStatus(), o.getUpdatedAt());
    }

    @Transactional
    public Page<OrderSummaryDto> adminList(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(o -> new OrderSummaryDto(o.getId(), o.getStatus(), o.getTotalAmountCents(), o.getCreatedAt()));
    }
}
