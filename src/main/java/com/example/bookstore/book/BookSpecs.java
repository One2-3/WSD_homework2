package com.example.bookstore.book;

import com.example.bookstore.author.Author;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecs {

    private BookSpecs() {}

    public static Specification<Book> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Book> hasSeller(Long sellerId) {
        if (sellerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("sellerId"), sellerId);
    }

    /** q: 제목/저자명 검색 (LIKE) */
    public static Specification<Book> matchesQ(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            // 중복 방지
            query.distinct(true);

            // title
            var titlePred = cb.like(cb.lower(root.get("title")), like);

            // author name via join
            Join<Book, BookAuthor> ba = root.join("bookAuthors", JoinType.LEFT);
            Join<BookAuthor, Author> a = ba.join("author", JoinType.LEFT);
            var authorPred = cb.like(cb.lower(a.get("name")), like);

            return cb.or(titlePred, authorPred);
        };
    }

    public static Specification<Book> hasAuthor(Long authorId) {
        if (authorId == null) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Book, BookAuthor> ba = root.join("bookAuthors", JoinType.INNER);
            return cb.equal(ba.get("author").get("id"), authorId);
        };
    }

    public static Specification<Book> hasCategory(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Book, BookCategory> bc = root.join("bookCategories", JoinType.INNER);
            return cb.equal(bc.get("category").get("id"), categoryId);
        };
    }
}
