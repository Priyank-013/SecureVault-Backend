package com.example.securevault.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() == null ? "Something went wrong" : ex.getMessage();

        HttpStatus status;

        if (message.toLowerCase().contains("locked")) {
            status = HttpStatus.TOO_MANY_REQUESTS;
        } else if (message.toLowerCase().contains("invalid")
                || message.toLowerCase().contains("not found")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (message.toLowerCase().contains("already exists")) {
            status = HttpStatus.CONFLICT;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status).body(message);
    }
}