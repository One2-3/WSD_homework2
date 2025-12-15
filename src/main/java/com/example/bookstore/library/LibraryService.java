package com.example.bookstore.library;

import com.example.bookstore.book.BookRepository;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.library.dto.LibraryItemDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;

    public LibraryService(LibraryRepository libraryRepository, BookRepository bookRepository) {
        this.libraryRepository = libraryRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<LibraryItemDto> list(Long userId, Pageable pageable) {
        return libraryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(x -> new LibraryItemDto(x.getBookId(), x.getCreatedAt()));
    }

    @Transactional
    public LibraryItemDto add(Long userId, Long bookId) {
        bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        if (libraryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 라이브러리에 존재합니다.");
        }

        LibraryItem item = new LibraryItem();
        item.setUserId(userId);
        item.setBookId(bookId);

        try {
            LibraryItem saved = libraryRepository.save(item);
            return new LibraryItemDto(saved.getBookId(), saved.getCreatedAt());
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 라이브러리에 존재합니다.");
        }
    }

    @Transactional
    public void remove(Long userId, Long bookId) {
        if (!libraryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "라이브러리 항목을 찾을 수 없습니다.");
        }
        libraryRepository.deleteByUserIdAndBookId(userId, bookId);
    }
}
