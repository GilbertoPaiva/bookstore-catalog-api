package com.gilbertopaiva.bookstore_catalog_api.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fields
) {
    public static ValidationErrorResponse of(String path, Map<String, String> fields) {
        return new ValidationErrorResponse(
                LocalDateTime.now(),
                422,
                "Unprocessable Entity",
                "Validation failed",
                path,
                fields
        );
    }
}

