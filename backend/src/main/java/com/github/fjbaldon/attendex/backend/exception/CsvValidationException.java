package com.github.fjbaldon.attendex.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CsvValidationException extends RuntimeException {
    public CsvValidationException(String message) {
        super(message);
    }
}
