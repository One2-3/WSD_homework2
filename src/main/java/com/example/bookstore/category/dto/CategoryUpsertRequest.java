package com.example.bookstore.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpsertRequest(
        @NotBlank(message = "name은 필수입니다.")
        String name
) {}
