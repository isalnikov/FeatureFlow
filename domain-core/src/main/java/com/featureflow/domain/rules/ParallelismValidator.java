package com.featureflow.domain.rules;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.entity.Sprint;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.valueobject.AssignmentStatus;

import java.util.List;
import java.util.UUID;

public final class ParallelismValidator {

    private static final int MAX_PARALLEL_FEATURES = 3;

    private ParallelismValidator() {}

    public static boolean withinParallelismLimit(
        Team team,
        Sprint sprint,
        List<Assignment> assignments
    ) {
        long activeFeatures = assignments.stream()
            .filter(a -> a.getTeamId().equals(team.getId()))
            .filter(a -> a.getSprintId().equals(sprint.id()))
            .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED)
            .map(Assignment::getFeatureId)
            .distinct()
            .count();

        return activeFeatures <= MAX_PARALLEL_FEATURES;
    }

    public static boolean withinParallelismLimit(
        Team team,
        Sprint sprint,
        List<Assignment> assignments,
        int maxParallel
    ) {
        long activeFeatures = assignments.stream()
            .filter(a -> a.getTeamId().equals(team.getId()))
            .filter(a -> a.getSprintId().equals(sprint.id()))
            .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED)
            .map(Assignment::getFeatureId)
            .distinct()
            .count();

        return activeFeatures <= maxParallel;
    }

    public static long countActiveFeatures(
        UUID teamId,
        UUID sprintId,
        List<Assignment> assignments
    ) {
        return assignments.stream()
            .filter(a -> a.getTeamId().equals(teamId))
            .filter(a -> a.getSprintId().equals(sprintId))
            .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED)
            .map(Assignment::getFeatureId)
            .distinct()
            .count();
    }
}
