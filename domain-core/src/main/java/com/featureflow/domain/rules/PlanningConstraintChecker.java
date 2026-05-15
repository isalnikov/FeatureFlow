package com.featureflow.domain.rules;

import com.featureflow.domain.entity.Assignment;
import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.entity.Team;
import com.featureflow.domain.planning.ConstraintViolation;
import com.featureflow.domain.planning.PlanningRequest;

import java.util.*;
import java.util.stream.Collectors;

public final class PlanningConstraintChecker {

    public List<ConstraintViolation> checkAll(
        PlanningRequest request,
        List<Assignment> proposedAssignments
    ) {
        List<ConstraintViolation> violations = new ArrayList<>();

        Map<UUID, Product> productMap = request.products().stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        Map<UUID, Team> teamMap = request.teams().stream()
            .collect(Collectors.toMap(Team::getId, t -> t));
        Map<UUID, FeatureRequest> featureMap = request.features().stream()
            .collect(Collectors.toMap(FeatureRequest::getId, f -> f));

        for (Assignment assignment : proposedAssignments) {
            Team team = teamMap.get(assignment.getTeamId());
            FeatureRequest feature = featureMap.get(assignment.getFeatureId());

            if (team == null || feature == null) continue;

            if (!OwnershipValidator.teamOwnsProducts(team, feature, productMap)) {
                violations.add(new ConstraintViolation(
                    ConstraintViolation.ViolationType.OWNERSHIP_VIOLATION,
                    feature.getId(), team.getId(),
                    "Team %s does not own all products for feature %s"
                        .formatted(team.getId(), feature.getId())
                ));
            }
        }

        try {
            Map<UUID, Set<UUID>> depGraph = request.features().stream()
                .collect(Collectors.toMap(
                    FeatureRequest::getId,
                    f -> new HashSet<>(f.getDependencies())
                ));
            DependencyValidator.checkNoCycles(depGraph);
        } catch (Exception e) {
            violations.add(new ConstraintViolation(
                ConstraintViolation.ViolationType.DEPENDENCY_CYCLE,
                null, null, e.getMessage()
            ));
        }

        return violations;
    }
}
