package com.example.bookstore.book;

import com.example.bookstore.category.Category;
import jakarta.persistence.*;

@Entity
@Table(name = "book_categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_book_categories_book_category", columnNames = {"book_id", "category_id"}))
public class BookCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected BookCategory() {}

    public BookCategory(Book book, Category category) {
        this.book = book;
        this.category = category;
    }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public Category getCategory() { return category; }
}
