package com.featureflow.domain.rules;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.entity.Sprint;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.valueobject.EffortEstimate;
import com.featureflow.domain.valueobject.Role;

import java.util.List;

public final class CapacityValidator {

    private CapacityValidator() {}

    public static ValidationResult checkCapacity(
        Team team,
        Sprint sprint,
        List<Assignment> existingAssignments,
        EffortEstimate newEffort
    ) {
        var capacity = team.effectiveCapacityForSprint(sprint);

        for (Role role : Role.values()) {
            double used = existingAssignments.stream()
                .filter(a -> a.getSprintId().equals(sprint.id()))
                .filter(a -> a.getTeamId().equals(team.getId()))
                .mapToDouble(a -> a.getAllocatedEffort().forRole(role))
                .sum();
            double available = capacity.effectiveHours(role);
            double needed = newEffort.forRole(role);

            if (needed > 0 && used + needed > available) {
                return ValidationResult.failure(
                    "Team %s exceeds %s capacity in sprint %s: %.1f + %.1f > %.1f"
                        .formatted(team.getId(), role, sprint.id(), used, needed, available)
                );
            }
        }
        return ValidationResult.ok();
    }

    public static ValidationResult checkCapacity(
        double availableHours,
        double usedHours,
        double neededHours,
        Role role
    ) {
        if (usedHours + neededHours > availableHours) {
            return ValidationResult.failure(
                "Role %s capacity exceeded: %.1f + %.1f > %.1f"
                    .formatted(role, usedHours, neededHours, availableHours)
            );
        }
        return ValidationResult.ok();
    }
}
