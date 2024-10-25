package com.interview.news.api.exception;

public class ExternalRateLimitExceededException extends RuntimeException {
    public ExternalRateLimitExceededException(String message) {
        super(message);
    }
}
