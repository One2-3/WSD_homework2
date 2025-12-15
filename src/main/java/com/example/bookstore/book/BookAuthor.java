package com.example.bookstore.book;

import com.example.bookstore.author.Author;
import jakarta.persistence.*;

@Entity
@Table(name = "book_authors",
        uniqueConstraints = @UniqueConstraint(name = "uk_book_authors_book_author", columnNames = {"book_id", "author_id"}))
public class BookAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    protected BookAuthor() {}

    public BookAuthor(Book book, Author author) {
        this.book = book;
        this.author = author;
    }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public Author getAuthor() { return author; }
}
