package com.featureflow.domain.exception;

public final class OwnershipViolationException extends DomainException {
    public OwnershipViolationException(String message) {
        super(message);
    }

    public OwnershipViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
