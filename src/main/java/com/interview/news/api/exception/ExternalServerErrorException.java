package com.interview.news.api.exception;

public class ExternalServerErrorException extends RuntimeException {
    public ExternalServerErrorException(String message) {
        super(message);
    }
}
