package com.example.bookstore.cart;

import com.example.bookstore.book.BookRepository;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.cart.dto.CartDtos.CartItemView;
import com.example.bookstore.cart.dto.CartDtos.CartView;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return cartRepository.save(c);
        });
    }

    @Transactional
    public CartView getCartView(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartIdOrderByIdDesc(cart.getId());
        return toView(items);
    }

    @Transactional
    public CartView addItem(Long userId, Long bookId, int quantity) {
        Cart cart = getOrCreateCart(userId);

        var snap = bookRepository.getForCartOrder(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        if (snap.getStock() < quantity) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "재고가 부족합니다.");
        }

        CartItem item = cartItemRepository.findByCartIdAndBookId(cart.getId(), bookId).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setCartId(cart.getId());
            item.setBookId(bookId);
            item.setUnitPriceCents(snap.getPriceCents());
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        } else {
            int newQty = item.getQuantity() + quantity;
            if (snap.getStock() < newQty) throw new ApiException(ErrorCode.VALIDATION_FAILED, "재고가 부족합니다.");
            item.setQuantity(newQty);
        }

        return getCartView(userId);
    }

    @Transactional
    public CartView patchItemQty(Long userId, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다."));

        if (!item.getCartId().equals(cart.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
        }

        var snap = bookRepository.getForCartOrder(item.getBookId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        if (snap.getStock() < quantity) throw new ApiException(ErrorCode.VALIDATION_FAILED, "재고가 부족합니다.");
        item.setQuantity(quantity);

        return getCartView(userId);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다."));

        if (!item.getCartId().equals(cart.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
        }
        cartItemRepository.delete(item);
    }


    @Transactional
    public List<CartItem> listItemsForCheckout(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.findByCartIdOrderByIdDesc(cart.getId());
    }

    @Transactional
    public void clear(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartView toView(List<CartItem> items) {
        int subtotal = 0;
        int totalQty = 0;
        List<CartItemView> views = new ArrayList<>();

        for (CartItem it : items) {
            int line = it.getUnitPriceCents() * it.getQuantity();
            subtotal += line;
            totalQty += it.getQuantity();
            views.add(new CartItemView(it.getId(), it.getBookId(), it.getQuantity(), it.getUnitPriceCents(), line));
        }
        return new CartView(views, subtotal, totalQty);
    }
}
