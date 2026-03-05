package com.gilbertopaiva.bookstore_catalog_api.book.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record BookRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Size(max = 255, message = "Author must not exceed 255 characters")
        String author,

        @NotBlank(message = "ISBN is required")
        @Size(max = 20, message = "ISBN must not exceed 20 characters")
        String isbn,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @PositiveOrZero(message = "Stock quantity must be zero or positive")
        Integer stockQuantity,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        Integer publishedYear,

        @NotNull(message = "Category ID is required")
        UUID categoryId
) {}

