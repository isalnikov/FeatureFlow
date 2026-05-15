package com.featureflow.domain.exception;

public sealed abstract class DomainException extends RuntimeException
    permits CapacityExceededException, DependencyCycleException, OwnershipViolationException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
