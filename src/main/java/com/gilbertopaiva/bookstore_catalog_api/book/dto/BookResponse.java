package com.gilbertopaiva.bookstore_catalog_api.book.dto;

import com.gilbertopaiva.bookstore_catalog_api.book.Book;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String author,
        String isbn,
        BigDecimal price,
        Integer stockQuantity,
        String description,
        Integer publishedYear,
        UUID categoryId,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getStockQuantity(),
                book.getDescription(),
                book.getPublishedYear(),
                book.getCategory().getId(),
                book.getCategory().getName(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}

