package com.interview.news.api.exception;

public class ExternalNotFoundException extends RuntimeException {
    public ExternalNotFoundException(String message) {
        super(message);
    }
}
