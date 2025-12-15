package com.example.bookstore.wishlist;

import com.example.bookstore.book.BookRepository;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.wishlist.dto.WishlistItemDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;

    public WishlistService(WishlistRepository wishlistRepository, BookRepository bookRepository) {
        this.wishlistRepository = wishlistRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<WishlistItemDto> list(Long userId, Pageable pageable) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(w -> new WishlistItemDto(w.getBookId(), w.getCreatedAt()));
    }

    @Transactional
    public WishlistItemDto add(Long userId, Long bookId) {
        bookRepository.findByIdAndDeletedAtIsNull(bookId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 위시리스트에 존재합니다.");
        }

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setBookId(bookId);

        try {
            WishlistItem saved = wishlistRepository.save(item);
            return new WishlistItemDto(saved.getBookId(), saved.getCreatedAt());
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 위시리스트에 존재합니다.");
        }
    }

    @Transactional
    public void remove(Long userId, Long bookId) {
        if (!wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "위시리스트 항목을 찾을 수 없습니다.");
        }
        wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
    }
}
