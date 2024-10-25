package com.interview.news.api.exception;

public class ExternalBadRequestException extends RuntimeException {
    public ExternalBadRequestException(String message) {
        super(message);
    }
}
