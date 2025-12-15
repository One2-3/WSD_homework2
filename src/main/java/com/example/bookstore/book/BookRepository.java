package com.example.bookstore.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIdAndDeletedAtIsNull(Long id);

    // Cart/Order용 스냅샷 프로젝션
    interface BookForCartOrder {
        Long getId();
        Long getSellerId();
        Integer getPriceCents();
        Integer getStock();
    }

    @Query(
        value = """
                select id as id,
                       seller_id as sellerId,
                       price_cents as priceCents,
                       stock as stock
                  from books
                 where id = :id
                   and deleted_at is null
                """,
        nativeQuery = true
    )
    Optional<BookForCartOrder> getForCartOrder(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
                update books
                   set stock = stock - :qty
                 where id = :id
                   and stock >= :qty
                """,
        nativeQuery = true
    )
    int decreaseStockIfEnough(@Param("id") Long id, @Param("qty") int qty);
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(
        value = """
                update books b
                set b.ratings_count = (select count(*) from reviews r where r.book_id = :bookId),
                    b.average_rating = (select ifnull(avg(r.rating), 0) from reviews r where r.book_id = :bookId)
                where b.id = :bookId
                """,
        nativeQuery = true
    )
    int refreshRatingStats(@org.springframework.data.repository.query.Param("bookId") Long bookId);

}
