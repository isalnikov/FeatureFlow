package com.featureflow.domain.exception;

public final class DependencyCycleException extends DomainException {
    public DependencyCycleException(String message) {
        super(message);
    }

    public DependencyCycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
