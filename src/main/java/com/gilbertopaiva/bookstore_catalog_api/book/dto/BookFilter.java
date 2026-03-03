package com.gilbertopaiva.bookstore_catalog_api.book.dto;

import java.math.BigDecimal;

public record BookFilter(
        String title,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {}

