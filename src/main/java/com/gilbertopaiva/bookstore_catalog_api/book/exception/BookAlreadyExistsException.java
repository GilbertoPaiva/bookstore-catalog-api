package com.gilbertopaiva.bookstore_catalog_api.book.exception;

public class BookAlreadyExistsException extends RuntimeException {
    public BookAlreadyExistsException(String isbn) {
        super("Book already exists with ISBN: " + isbn);
    }
}
