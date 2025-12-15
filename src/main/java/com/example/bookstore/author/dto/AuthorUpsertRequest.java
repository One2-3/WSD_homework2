package com.example.bookstore.author.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthorUpsertRequest(
        @NotBlank(message = "name은 필수입니다.")
        String name
) {}
