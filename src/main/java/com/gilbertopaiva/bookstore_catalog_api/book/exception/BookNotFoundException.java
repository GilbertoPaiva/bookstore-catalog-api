package com.gilbertopaiva.bookstore_catalog_api.book.exception;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(UUID id) {
        super("Book not found with id: " + id);
    }
}

