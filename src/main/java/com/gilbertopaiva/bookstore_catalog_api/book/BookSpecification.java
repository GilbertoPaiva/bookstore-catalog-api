package com.gilbertopaiva.bookstore_catalog_api.book;

import com.gilbertopaiva.bookstore_catalog_api.book.dto.BookFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    private BookSpecification() {}

    public static Specification<Book> withFilters(BookFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por título — LIKE case insensitive
            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + filter.title().toLowerCase() + "%"
                ));
            }

            // Filtro por categoria
            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }

            // Filtro por faixa de preço
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

