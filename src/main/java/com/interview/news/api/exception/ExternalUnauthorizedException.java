package com.interview.news.api.exception;

public class ExternalUnauthorizedException extends RuntimeException {
    public ExternalUnauthorizedException(String message) {
        super(message);
    }
}
