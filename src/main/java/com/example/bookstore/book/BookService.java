package com.example.bookstore.book;

import com.example.bookstore.author.Author;
import com.example.bookstore.author.AuthorRepository;
import com.example.bookstore.book.dto.*;
import com.example.bookstore.category.Category;
import com.example.bookstore.category.CategoryRepository;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.sellers.SellerRepository;
import com.example.bookstore.user.User;
import com.example.bookstore.user.UserRepository;
import com.example.bookstore.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final SellerRepository sellerRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public BookService(
            BookRepository bookRepository,
            SellerRepository sellerRepository,
            AuthorRepository authorRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository
    ) {
        this.bookRepository = bookRepository;
        this.sellerRepository = sellerRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ---------------------------
    // Public
    // ---------------------------
    @Transactional(readOnly = true)
    public Page<BookSummaryDto> listPublic(String q, Long sellerId, Long authorId, Long categoryId, Pageable pageable) {
        Specification<Book> spec = BookSpecs.notDeleted()
                .and(BookSpecs.matchesQ(q))
                .and(BookSpecs.hasSeller(sellerId))
                .and(BookSpecs.hasAuthor(authorId))
                .and(BookSpecs.hasCategory(categoryId));

        return bookRepository.findAll(spec, pageable)
                .map(b -> new BookSummaryDto(b.getId(), b.getTitle(), b.getPriceCents()));
    }

    @Transactional(readOnly = true)
    public Book requireActive(Long bookId) {
        return bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public BookDetailDto detail(Long bookId) {
        return toDetail(requireActive(bookId));
    }

    // ---------------------------
    // Admin
    // ---------------------------
    @Transactional
    public Book createAdmin(BookCreateRequest req) {
        sellerRepository.findByIdAndDeletedAtIsNull(req.seller_id())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "판매자를 찾을 수 없습니다."));

        Book b = new Book();
        b.setSellerId(req.seller_id());
        b.setTitle(req.title());
        b.setPriceCents(req.price_cents());
        b.setStock(req.stock());
        b.setAverageRating(null);
        b.setRatingsCount(0);

        bookRepository.save(b);

        replaceAuthorsRequired(b, req.author_ids());
        replaceCategoriesRequired(b, req.category_ids());

        return bookRepository.save(b);
    }

    @Transactional
    public Book patchAdmin(Long bookId, BookPatchRequest req) {
        Book b = requireActive(bookId);

        if (req.title() != null) b.setTitle(req.title());
        if (req.price_cents() != null) b.setPriceCents(req.price_cents());
        if (req.stock() != null) b.setStock(req.stock());

        if (req.author_ids() != null) replaceAuthorsRequired(b, req.author_ids());
        if (req.category_ids() != null) replaceCategoriesRequired(b, req.category_ids());

        return bookRepository.save(b);
    }

    @Transactional
    public void softDeleteAdmin(Long bookId) {
        Book b = requireActive(bookId);
        b.setDeletedAt(Instant.now());
        bookRepository.save(b);
    }

    // ---------------------------
    // Seller
    // ---------------------------

    /**
     * 판매자 본인 도서 목록 (공개 목록 로직을 재사용하되, sellerId를 강제)
     */
    @Transactional(readOnly = true)
    public Page<BookSummaryDto> listForSeller(Long sellerUserId, String q, Pageable pageable) {
        User me = userRepository.findByIdAndDeletedAtIsNull(sellerUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));

        if (me.getRole() != UserRole.seller || me.getSellerId() == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "판매자 권한이 없습니다.");
        }

        return listPublic(q, me.getSellerId(), null, null, pageable);
    }
    @Transactional
    public Book createForSeller(Long sellerUserId, SellerBookCreateRequest req) {
        User me = userRepository.findByIdAndDeletedAtIsNull(sellerUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));

        if (me.getRole() != UserRole.seller) {
            throw new ApiException(ErrorCode.FORBIDDEN, "판매자 권한이 없습니다.");
        }
        if (me.getSellerId() == null) {
            throw new ApiException(ErrorCode.CONFLICT, "판매자 계정에 seller_id가 연결되어 있지 않습니다.");
        }

        sellerRepository.findByIdAndDeletedAtIsNull(me.getSellerId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "판매자를 찾을 수 없습니다."));

        Book b = new Book();
        b.setSellerId(me.getSellerId());
        b.setTitle(req.title());
        b.setPriceCents(req.price_cents());
        b.setStock(req.stock());
        b.setAverageRating(null);
        b.setRatingsCount(0);

        bookRepository.save(b);

        // 선택: null/[] 이면 연결 안 함(그대로 비움)
        replaceAuthorsOptional(b, req.author_ids());
        replaceCategoriesOptional(b, req.category_ids());

        return bookRepository.save(b);
    }

    @Transactional
    public Book patchForSeller(Long sellerUserId, Long bookId, BookPatchRequest req) {
        User me = userRepository.findByIdAndDeletedAtIsNull(sellerUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));

        if (me.getRole() != UserRole.seller || me.getSellerId() == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "판매자 권한이 없습니다.");
        }

        Book b = requireActive(bookId);
        if (!Objects.equals(b.getSellerId(), me.getSellerId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인 판매자의 도서만 수정할 수 있습니다.");
        }

        if (req.title() != null) b.setTitle(req.title());
        if (req.price_cents() != null) b.setPriceCents(req.price_cents());
        if (req.stock() != null) b.setStock(req.stock());

        // 선택: null이면 무시, []면 전부 제거
        if (req.author_ids() != null) replaceAuthorsAllowEmpty(b, req.author_ids());
        if (req.category_ids() != null) replaceCategoriesAllowEmpty(b, req.category_ids());

        return bookRepository.save(b);
    }

    @Transactional
    public void softDeleteForSeller(Long sellerUserId, Long bookId) {
        User me = userRepository.findByIdAndDeletedAtIsNull(sellerUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));

        if (me.getRole() != UserRole.seller || me.getSellerId() == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "판매자 권한이 없습니다.");
        }

        Book b = requireActive(bookId);
        if (!Objects.equals(b.getSellerId(), me.getSellerId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인 판매자의 도서만 삭제할 수 있습니다.");
        }

        b.setDeletedAt(Instant.now());
        bookRepository.save(b);
    }

    // ---------------------------
    // Mapping
    // ---------------------------
    @Transactional(readOnly = true)
    public BookDetailDto toDetail(Book b) {
        List<NamedIdDto> authors = b.getBookAuthors().stream()
                .map(ba -> new NamedIdDto(ba.getAuthor().getId(), ba.getAuthor().getName()))
                .distinct()
                .toList();

        List<NamedIdDto> categories = b.getBookCategories().stream()
                .map(bc -> new NamedIdDto(bc.getCategory().getId(), bc.getCategory().getName()))
                .distinct()
                .toList();

        return new BookDetailDto(
                b.getId(),
                b.getSellerId(),
                b.getTitle(),
                b.getPriceCents(),
                b.getStock(),
                b.getAverageRating(),
                b.getRatingsCount(),
                authors,
                categories,
                b.getUpdatedAt()
        );
    }

    // ---------------------------
    // Relation helpers
    // ---------------------------
    private void replaceAuthorsRequired(Book b, List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "author_ids는 1개 이상이어야 합니다.",
                    Map.of("author_ids", "empty"));
        }
        replaceAuthorsAllowEmpty(b, authorIds);
    }

    private void replaceCategoriesRequired(Book b, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "category_ids는 1개 이상이어야 합니다.",
                    Map.of("category_ids", "empty"));
        }
        replaceCategoriesAllowEmpty(b, categoryIds);
    }

    private void replaceAuthorsOptional(Book b, List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) return;
        replaceAuthorsAllowEmpty(b, authorIds);
    }

    private void replaceCategoriesOptional(Book b, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return;
        replaceCategoriesAllowEmpty(b, categoryIds);
    }

    /**
     * list가 비어있으면 전부 제거, 있으면 교체.
     */
    private void replaceAuthorsAllowEmpty(Book b, List<Long> authorIds) {
        b.getBookAuthors().clear();
        if (authorIds == null || authorIds.isEmpty()) return;

        Map<Long, Author> authors = authorRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Author::getId, a -> a));
        for (Long id : authorIds) {
            if (!authors.containsKey(id)) {
                throw new ApiException(ErrorCode.NOT_FOUND, "저자를 찾을 수 없습니다.", Map.of("author_id", id));
            }
        }

        LinkedHashSet<Long> uniq = new LinkedHashSet<>(authorIds);
        for (Long id : uniq) {
            b.getBookAuthors().add(new BookAuthor(b, authors.get(id)));
        }
    }

    /**
     * list가 비어있으면 전부 제거, 있으면 교체.
     */
    private void replaceCategoriesAllowEmpty(Book b, List<Long> categoryIds) {
        b.getBookCategories().clear();
        if (categoryIds == null || categoryIds.isEmpty()) return;

        Map<Long, Category> cats = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
        for (Long id : categoryIds) {
            if (!cats.containsKey(id)) {
                throw new ApiException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다.", Map.of("category_id", id));
            }
        }

        LinkedHashSet<Long> uniq = new LinkedHashSet<>(categoryIds);
        for (Long id : uniq) {
            b.getBookCategories().add(new BookCategory(b, cats.get(id)));
        }
    }
}
