package com.gilbertopaiva.bookstore_catalog_api.book.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BookFilter(
        String title,
        UUID categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {}

