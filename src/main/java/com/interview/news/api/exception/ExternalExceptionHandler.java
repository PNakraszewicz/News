package com.interview.news.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExternalExceptionHandler {

    @ExceptionHandler(ExternalUnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(ExternalUnauthorizedException ex) {
        return new ResponseEntity<>("Unauthorized: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExternalBadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(ExternalBadRequestException ex) {
        return new ResponseEntity<>("Bad Request: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExternalRateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceededException(ExternalRateLimitExceededException ex) {
        return new ResponseEntity<>("Rate limit exceeded: " + ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(ExternalServerErrorException.class)
    public ResponseEntity<String> handleServerErrorException(ExternalServerErrorException ex) {
        return new ResponseEntity<>("Internal server error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExternalNotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(ExternalNotFoundException ex) {
        return new ResponseEntity<>("Not Found: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>("An internal server error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
