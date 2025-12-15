
package com.example.bookstore.stats;

import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.stats.dto.StatsDtos.*;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final StatsService statsService;

    public AdminStatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/daily-sales")
    public ResponseEntity<ApiResponse<DailySalesPayload>> dailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", statsService.dailySales(from, to)));
    }

    @GetMapping("/top-books")
    public ResponseEntity<ApiResponse<TopBooksPayload>> topBooks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "10") @Min(1) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", statsService.topBooks(from, to, limit)));
    }

    @GetMapping("/top-sellers")
    public ResponseEntity<ApiResponse<TopSellersPayload>> topSellers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "10") @Min(1) int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok("OK", statsService.topSellers(from, to, limit)));
    }
}
