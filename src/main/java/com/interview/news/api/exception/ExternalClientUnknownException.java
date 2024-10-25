package com.interview.news.api.exception;

import org.springframework.web.client.RestClientException;

public class ExternalClientUnknownException extends RuntimeException {
    public ExternalClientUnknownException(String message, RestClientException e) {
        super(message);
    }
}
