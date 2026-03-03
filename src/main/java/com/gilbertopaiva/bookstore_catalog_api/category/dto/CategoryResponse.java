package com.gilbertopaiva.bookstore_catalog_api.category.dto;

import com.gilbertopaiva.bookstore_catalog_api.category.Category;

public record CategoryResponse(
        Long id,
        String name,
        String description
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}

