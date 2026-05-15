package com.featureflow.domain.planning;

import java.util.UUID;

public record Conflict(
    Type type,
    UUID featureId,
    UUID teamId,
    String message
) {
    public enum Type {
        CAPACITY_EXCEEDED,
        DEPENDENCY_VIOLATION,
        OWNERSHIP_VIOLATION,
        PARALLELISM_EXCEEDED,
        DEADLINE_MISSED,
        DEPENDENCY_CYCLE
    }
}
