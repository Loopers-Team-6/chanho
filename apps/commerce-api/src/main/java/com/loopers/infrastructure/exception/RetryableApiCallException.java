package com.loopers.infrastructure.exception;

public class RetryableApiCallException extends RuntimeException {
    public RetryableApiCallException(Throwable cause) {
        super(cause);
    }
}
