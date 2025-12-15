
package com.example.bookstore.stats;

import com.example.bookstore.stats.dto.StatsDtos.*;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class StatsService {

    private final JdbcTemplate jdbcTemplate;

    public StatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public DailySalesPayload dailySales(LocalDate from, LocalDate to) {
        LocalDate endPlus = to.plusDays(1);
        List<DailySalesDto> items = jdbcTemplate.query(
                """
                select date(o.created_at) as d,
                       sum(o.total_amount_cents) as gross,
                       count(*) as cnt
                  from orders o
                 where o.created_at >= ?
                   and o.created_at < ?
                   and o.status in ('paid','shipped','delivered')
                 group by date(o.created_at)
                 order by d asc
                """,
                (rs, rowNum) -> new DailySalesDto(
                        rs.getDate("d").toLocalDate(),
                        rs.getInt("gross"),
                        rs.getInt("cnt")
                ),
                Date.valueOf(from), Date.valueOf(endPlus)
        );
        return new DailySalesPayload(items);
    }

    @Transactional
    public TopBooksPayload topBooks(LocalDate from, LocalDate to, int limit) {
        int l = Math.min(Math.max(limit, 1), 50);
        LocalDate endPlus = to.plusDays(1);
        List<TopBookDto> items = jdbcTemplate.query(
                """
                select oi.book_id as book_id,
                       b.title as title,
                       sum(oi.quantity) as sold_qty,
                       sum(oi.subtotal_cents) as gross
                  from order_items oi
                  join orders o on o.id = oi.order_id
                  join books b on b.id = oi.book_id
                 where o.created_at >= ?
                   and o.created_at < ?
                   and o.status in ('paid','shipped','delivered')
                 group by oi.book_id, b.title
                 order by gross desc
                 limit ?
                """,
                (rs, rowNum) -> new TopBookDto(
                        rs.getLong("book_id"),
                        rs.getString("title"),
                        rs.getInt("sold_qty"),
                        rs.getInt("gross")
                ),
                Date.valueOf(from), Date.valueOf(endPlus), l
        );
        return new TopBooksPayload(items);
    }

    @Transactional
    public TopSellersPayload topSellers(LocalDate from, LocalDate to, int limit) {
        int l = Math.min(Math.max(limit, 1), 50);
        LocalDate endPlus = to.plusDays(1);
        List<TopSellerDto> items = jdbcTemplate.query(
                """
                select oi.seller_id as seller_id,
                       s.name as name,
                       sum(oi.subtotal_cents) as gross
                  from order_items oi
                  join orders o on o.id = oi.order_id
                  join sellers s on s.id = oi.seller_id
                 where o.created_at >= ?
                   and o.created_at < ?
                   and o.status in ('paid','shipped','delivered')
                 group by oi.seller_id, s.name
                 order by gross desc
                 limit ?
                """,
                (rs, rowNum) -> new TopSellerDto(
                        rs.getLong("seller_id"),
                        rs.getString("name"),
                        rs.getInt("gross")
                ),
                Date.valueOf(from), Date.valueOf(endPlus), l
        );
        return new TopSellersPayload(items);
    }
}
