package com.example.greenhouse.exceptions.auth;

public class InvalidTokenTypeException extends RuntimeException {
    public InvalidTokenTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
