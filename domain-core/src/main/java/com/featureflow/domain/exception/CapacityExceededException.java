package com.featureflow.domain.exception;

public final class CapacityExceededException extends DomainException {
    public CapacityExceededException(String message) {
        super(message);
    }

    public CapacityExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
