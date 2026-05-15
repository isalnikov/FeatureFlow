package com.featureflow.domain.entity;

import com.featureflow.domain.valueobject.Role;

import java.util.UUID;

public record TeamMember(
    UUID personId,
    String name,
    Role role,
    double availabilityPercent
) {
    public TeamMember {
        if (availabilityPercent < 0 || availabilityPercent > 1) {
            throw new IllegalArgumentException("Availability must be in [0, 1]");
        }
    }

    public TeamMember(UUID personId, String name, Role role) {
        this(personId, name, role, 1.0);
    }
}
