package com.example.bookstore.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public class ReviewDtos {

    public record CreateReviewRequest(
            @NotNull @Min(1) @Max(5) Integer rating,
            @NotBlank String body
    ) {}

    public record PatchReviewRequest(
            @NotNull @Min(1) @Max(5) Integer rating,
            @NotBlank String body
    ) {}

    public record ReviewDto(
            Long id,
            Long user_id,
            Long book_id,
            Integer rating,
            String body,
            Integer like_count,
            boolean liked,
            Instant created_at,
            Instant updated_at
    ) {}

    public record LikePayload(boolean liked, Integer like_count) {}

    public record TopReviewsPayload(List<ReviewDto> items) {}
}
