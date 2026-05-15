package com.featureflow.domain.planning;

import java.util.UUID;

public record ConstraintViolation(
    ViolationType type,
    UUID featureId,
    UUID teamId,
    String message
) {
    public enum ViolationType {
        CAPACITY_EXCEEDED,
        DEPENDENCY_VIOLATION,
        OWNERSHIP_VIOLATION,
        PARALLELISM_EXCEEDED,
        DEADLINE_MISSED,
        DEPENDENCY_CYCLE
    }
}
