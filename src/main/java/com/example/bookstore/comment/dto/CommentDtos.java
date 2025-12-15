package com.example.bookstore.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CommentDtos {

    public record CreateCommentRequest(@NotBlank String body) {}
    public record PatchCommentRequest(@NotBlank String body) {}

    public record CommentDto(
            Long id,
            Long review_id,
            Long user_id,
            String body,
            Integer like_count,
            boolean liked,
            Instant created_at,
            Instant updated_at
    ) {}

    public record LikePayload(boolean liked, Integer like_count) {}
}
